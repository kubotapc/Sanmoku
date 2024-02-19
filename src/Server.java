import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Server {

	// 一回の手番の制限時間 （ミリ秒）
	private static long TIMEOUT = 1000; // msec

	private int nplayer;
	private int nstone;
	private int board_size;
	private int nsquare;
	private int ngame;

	private Player[] players;
	private String[] player_names;
	private String[] player_classnames;

	private int[] board;

	private int[] current_score;
	private int[] game_score;

	public boolean player_disabled[];

	// プレーヤ向けの public methods

	public int[] get_current_score() {
		return current_score.clone();
	}

	public int[] get_game_score() {
		return game_score.clone();
	}

	public int[] get_current_board() {
		return board.clone();
	}

	public String[] get_player_names() {
		return player_names.clone();
	}

	public int wait_jikkyo_click() {
		int result = -1;
		if (jikkyo != null) {
			result = jikkyo.wait_click();
		}
		return result;
	}

	/**
	 * プレーヤが持ってきた盤面が正しい盤面であるかチェック （サイズ、石の種類）
	 * 
	 * @param bd プレーヤが持ってきた盤面配列
	 */
	private void validate_board(int bd[]) {
		boolean bad = false;
		if (bd == null)
			bad = true;
		else if (bd.length != nsquare)
			bad = true;
		else {
			for (int i = 0; i < nsquare; i++) {
				if ((bd[i] < -1) || (bd[i] >= nplayer)) {
					bad = true;
					break;
				}
			}
		}
		if (bad) {
			throw new AssertionError("validate_board");
		}
	}

	public int[] evaluate_board(int bd[]) {
		validate_board(bd);
		return calc_score(bd);
	}

	public void dump_board(int bd[]) {
		validate_board(bd);
		dump_board_priv(bd);
	}

	// ここまでプレーヤ向けの public methods

	boolean enable_jikkyo = false;
	boolean enable_userConsole = true;

	/**
	 * @param args
	 * @return number of consumed elements in args[]
	 */
	private int init_option(String[] args) {
		int result = 0;
		if (args.length > 0) {
			if (args[0].equals("-j")) {
				enable_jikkyo = true;
				result++;
				if (args[1].equals("-nc")) {
					enable_userConsole = false;
					result++;
				}
			}
		}
		return result;
	}

	private void init_player(String[] args, int nconsumed) {
		nplayer = args.length - nconsumed;

		nstone = 10;
		board_size = (int) Math.ceil(Math.sqrt((double) nstone * (double) nplayer));
		nsquare = board_size * board_size;
		ngame = nplayer;
		game_score = new int[nplayer];

		players = new Player[nplayer];
		player_names = new String[nplayer];
		player_classnames = new String[nplayer];

		if (nplayer <= 0) {
			throw (new Error("No players"));
		}

		int id = 0;
		for (int i = nconsumed; i < args.length; i++) {
			String name = args[i];
			player_classnames[id] = name;
			System.out.println("PlayerInit:" + name);
			try {
				Player p;
				if (name.charAt(0) == '*') {
					p = new Human(name.substring(1));
				} else {
					p = (Player) Class.forName(name).getDeclaredConstructor().newInstance();
				}
				players[id] = p;
				player_names[id] = p.init(id, nplayer, nstone, ngame, board_size, this);
				id++;
			} catch (ClassNotFoundException e) {
				System.err.println(e);
			} catch (InstantiationException e) {
				System.err.println(e);
			} catch (IllegalAccessException e) {
				System.err.println(e);
			} catch (NoSuchMethodException e) {
				System.err.println(e);
			} catch (InvocationTargetException e) {
				System.err.println(e);
			}
		}
		if (id != nplayer) {
			throw (new Error("Not all players loaded"));
		}
	}

	private void game_loop() {
		for (int g = 0; g < ngame; g++) {
			game(g);
			dump_score();
			if (jikkyo != null)
				jikkyo.endgame();
			for (int i = 0; i < nplayer; i++) {
				game_score[i] += current_score[i];
			}
			current_score = new int[nplayer];
		}
	}

	/**
	 * 着手が正当か否かの判定 （空でないマスへの着手、縦横隣接制限、四目禁止）
	 * 
	 * @param id  石を置こうとしているユーザのid
	 * @param pos 置こうとしている場所
	 * @return ルール違反があったら true
	 */
	private boolean play_ng(int id, int pos) {
		// 既に石がある
		if (board[pos] != -1)
			return true;

		// 上下左右のどこにも隣接する石がない
		if (((pos % board_size == 0) || (board[pos - 1] == -1)) &&
				((pos % board_size == board_size - 1) || (board[pos + 1] == -1)) &&
				((pos < board_size) || (board[pos - board_size] == -1)) &&
				((pos >= board_size * (board_size - 1)) || (board[pos + board_size] == -1)))
			return true;

		// 4目の連鎖は禁止
		// 仮に置いてみてスコア計算してみる
		int tmp_board[] = board.clone();
		tmp_board[pos] = id;
		int sc[] = calc_score(tmp_board);
		if (sc[id] < 0)
			return true;

		// 問題なし
		return false;
	}

	/**
	 * プレーヤ p の turn メソッドを呼び出して着手をもらう
	 * もらった着手が盤外だったら AssertionError を投げる
	 * 
	 * @param p     プレーヤオブジェクト
	 * @param again 不正着手による再呼び出しかどうかのフラグ
	 * @return 盤面内への着手 （正当かどうかは未チェック）
	 */
	// private int turn(Player p, boolean again){
	// int r = p.turn(again);
	// if((r >= 0) && (r < nsquare)){
	// return r;
	// } else {
	// String msg = String.format("Illegal move: %s", r);
	// throw new AssertionError(msg);
	// }
	// }
	private int turn(Player p, boolean again) {
		Executor executor = new Executor();
		int r = -1;
		if (p.isHuman()) {
			r = p.turn(again);
		} else {
			String msg=null;
			try {
				r = executor.exec((UserClass) p, again);
			} catch (TimeoutException e) {
				msg = String.format("ERROR!(TIMEOUT/ infinity loop): %s%n", p);

			} catch (Exception e) {
				msg = String.format("ERROR!(internal failure): %s%n", p);
				msg += String.format("--- %s%n", e.getCause().getMessage());
				r = -999;
			} finally {
				if (msg !=null) {
					if (enable_jikkyo) {
						errConsole.addText(msg);
					}
					throw new AssertionError(msg);
				}
			}
		}
		if ((r >= 0) && (r < nsquare)) {
			return r;
		} else {
			String msg;
			// if (r == -999) {
			// 	msg = String.format("ERROR!(internal failure): %s%n", p);
			// } else if (r == -998) {
			// 	msg = String.format("ERROR!(TIMEOUT/ infinity loop): %s%n", p);
			// } else if (r == -997) {
			// 	msg = String.format("ERROR!(array out of bounds): %s%n", p);
			// } else {
				msg = String.format("ERROR!(Illegal position(%d) set): %s%n", r, p);
			// }
			if (enable_jikkyo) {
				errConsole.addText(msg);
			}
			throw new AssertionError(msg);
		}
	}

	private volatile int result;

	/**
	 * プレーヤの turn メソッドを呼び出し、結果を返す
	 * 指定した時間を超過したら強制終了して -1 を返す
	 * 例外を発生したりアサーションに失敗しても -1 を返す
	 * 合法な着手でない場合、繰り返して turn を呼び出す
	 * 
	 * @param nturn  手番 (初手: 0)
	 * @param id     対象となるプレーヤのid
	 * @param millis 制限時間 (ミリ秒)
	 * @return 0 以上 : 合法的な着手、 -1 : エラー
	 */
	private int player_turn(final int nturn, final int id, final long millis) {
		final Player p = players[id];
		final TaskThread taskThread = new TaskThread() {
			@Override
			public void run() {
				try {
					int tmp_result;
					tmp_result = turn(p, false);
					// 第一手目以外は置ける場所に制限があるのでチェックし、ダメなら尋ね直す
					while ((nturn > 0) && play_ng(id, tmp_result)) {
						tmp_result = turn(p, true);
						if (this.toBeKilled) {
							if (enable_jikkyo) {
								errConsole.addText(
										String.format("ERROR (same position(%d) answer repeat): %s%n", tmp_result, p));
							}
							result = -2;
							return;
						}
					}
					result = tmp_result;
				} catch (RuntimeException ex) {
					System.err.format("ERROR: %s%n", p);
					ex.printStackTrace();
					if (enable_jikkyo) {
						errConsole.addText(String.format("ERROR (internal failure): %s%n", p));
					}
					result = -1;
				} catch (AssertionError ex) {
					System.err.format("ERROR: %s%n", p);
					ex.printStackTrace();
					result = -1;
				}
			}
		};

		result = -2;
		Thread killThread = null;

		if (millis > 0) {
			killThread = new Thread() {
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					try {
						Thread.sleep(millis);
						// Thread.sleep(1500);
						if (result == -2) {
							// taskThread.stop();
							taskThread.toBeKilled = true;
							System.err.format("ERROR (TIMEOUT): %s%n", p);
							result = -1;
						}
					} catch (InterruptedException e) {
					}
				}
			};

			killThread.start();
		}

		taskThread.start();

		try {
			taskThread.join();
			if (killThread != null) {
				killThread.interrupt();
				killThread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void game(int game_no) {
		board = new int[nsquare];
		Arrays.fill(board, -1);
		current_score = calc_score();

		player_disabled = new boolean[nplayer];

		int n = 0;
		int start_player = game_no;

		for (int i = 0; i < nplayer; i++) {
			players[i].newgame(start_player);
		}

		if (jikkyo != null)
			jikkyo.newgame(start_player);

		for (int s = 0; s < nstone; s++) {
			for (int p = 0; p < nplayer; p++) {
				int id = (p + start_player) % nplayer;
				if (!player_disabled[id]) {
					long timeout = TIMEOUT;
					if (players[id].isHuman()) {
						timeout = 0;
					}
					if (jikkyo != null)
						jikkyo.turn_start(id, nstone - s);
					int pos = player_turn(n, id, timeout);
					if (pos < 0) {
						// エラーがあったので、このゲームは以降おやすみ
						player_disabled[id] = true;
					} else {
						play_stone(id, pos);
						current_score = calc_score();
						if (jikkyo != null)
							jikkyo.play_stone(id, pos);
						n++;
					}
				}
				if (jikkyo == null) {
					dump();
				}
			}
		}
	}

	private void play_stone(int id, int pos) {
		board[pos] = id;
	}

	private void calc_score_sub(int p, int n, int delta, int score[], int bd[]) {
		int last_id = -1;
		int same_count = 0;
		while (n > 0) {
			int v = bd[p];
			if (v != last_id) {
				same_count = 1;
				last_id = v;
			} else {
				if (v >= 0) {
					same_count++;
					if (same_count == 3)
						score[v]++;
					else if (same_count == 4)
						score[v] -= 1000;
				}
			}
			p += delta;
			n--;
		}
	}

	private int[] calc_score() {
		return calc_score(board);
	}

	private int[] calc_score(int bd[]) {
		int score[] = new int[nplayer];
		for (int i = 0; i < board_size; i++) {
			// 横方向
			calc_score_sub(i * board_size, board_size, 1, score, bd);
			// 縦方向
			calc_score_sub(i, board_size, board_size, score, bd);
			// 右下方向(の上半分)
			calc_score_sub(i, board_size - i, board_size + 1, score, bd);
			// 左下方向(の上半分)
			calc_score_sub(i, i + 1, board_size - 1, score, bd);
		}

		for (int i = 1; i < board_size; i++) {
			// 右下方向(の下半分)
			calc_score_sub(i * board_size, board_size - i, board_size + 1, score, bd);
			// 左下方向(の下半分)
			calc_score_sub(i * board_size + board_size - 1, board_size - i, board_size - 1, score, bd);
		}

		return score;
	}

	private void dump() {
		dump_board_priv();
		dump_score();
	}

	private void dump_board_priv() {
		dump_board_priv(board);
	}

	private void dump_board_priv(int board[]) {
		for (int x = 0; x < board_size; x++) {
			System.out.print("---");
		}
		System.out.println("");
		for (int y = 0; y < board_size; y++) {
			for (int x = 0; x < board_size; x++) {
				int v = board[y * board_size + x];
				if (v == -1) {
					System.out.print(" . ");
				} else {
					System.out.format("%2d ", v);
				}
			}
			System.out.println("");
		}
	}

	private void dump_score() {
		for (int i = 0; i < nplayer; i++)
			System.out.printf("%d ", current_score[i]);
		System.out.print("/ ");
		for (int i = 0; i < nplayer; i++)
			System.out.printf("%d ", game_score[i]);
		System.out.println("");
	}

	private void dump_game_score() {
		for (int i = 0; i < nplayer; i++)
			System.out.printf("%d ", game_score[i]);
		System.out.println("");
	}

	Jikkyo jikkyo;
	UserConsole errConsole;

	private void init_jikkyo() {
		jikkyo = new Jikkyo(this, nplayer, board_size, player_names);
		errConsole = new UserConsole(-1, "ErrorConsole", this);
		errConsole.setBounds(1050, 40, 350, 300);
		errConsole.textArea.setForeground(Color.ORANGE);
		errConsole.textArea.setBackground(Color.DARK_GRAY);
		errConsole.setVisible(true);
	}

	private void start(String[] args) {
		int x = init_option(args);
		System.out.println("x:" + x);
		init_player(args, x);
		if (enable_jikkyo) {
			init_jikkyo();
		}
		game_loop();
		dump_game_score();
		if (jikkyo != null) {
			jikkyo.endmatch();
			for (Player p : players) {
				p.myConsole.dispose();
			}
			errConsole.dispose();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.start(args);
	}

}
