import java.util.Random;

public class one extends Player {
	Random random = new Random();
	int pos;
	int turns; // 何手目かを管理する変数
	int i; // 箱の最後尾
	int j; // 箱のカウンター
	int box[] = new int[20]; // 自ゴマ用

	boolean firstPlay = false;

	public one() {
		player_name = "one"; // 名前
	}

	// game作成時実行される
	@Override
	void newgame(int firstplayer) {
		turns = 0; // 手数を初期化
		myConsole.clear(); // デバッグ用コンソールを初期化
		if (id == firstplayer) {
			firstPlay = true; // 初手が打てる場合は初手フラグを立てる
		} else {
			firstPlay = false; // それ以外は初手フラグを下ろす
		}
	}

	// 自分のターンで実行
	@Override
	public int turn(boolean again) {
		// 通常
		if (!again) {
			turns++;
			consoleOutln(" pos:" + pos);
			pos = 0; // 探索する場所を左上に設定する
		}
		// again
		else {
			box[turns - 1] = -1;
			int rad = random.nextInt(board_size * board_size); // randomをradに入れる
			box[turns - 1] = rad;
			return (rad); // ランダムに置く
		}

		consoleOut(again ? " again " : "turns:" + turns + " ");
		consoleOutln(" pos:" + pos);
		for (i = 0; i < 20; i++) {
			consoleOutln(" box:" + box[i]);
		}
		int[] board = server.get_current_board(); // ボードの状態を取得

		// 自分たちのターン1回目
		if (turns == 1) {
			for (i = 0; i < 20; i++) { // 初期化
				box[turns - 1] = -1;
			}
			// 盤面真っ白の場合 完全初ターン
			if (firstPlay == true) {
				firstPlay = false; // 初手フラグを下ろす
				if (board_size * board_size - 3 > 0) {
					box[turns - 1] = (board_size * (board_size - 3) + (board_size - 3)); // 自コマ入れる
					return (board_size * (board_size - 3) + (board_size - 3)); // 端から2マス離す
				} else {
					box[turns - 1] = (board_size / 2 * board_size + board_size / 2); // 自コマ入れる
					return (board_size / 2 * board_size + board_size / 2); // 万が一エラーの時，真ん中おく
				}
			}
			// 盤面が汚い 初ターン
			else {
				int rad = random.nextInt(board_size * board_size); // randomをradに入れる
				box[turns - 1] = rad;
				return (rad); // ランダムに置く
			}
		}

		// 1ターン目以外
		// 三目 i=-1の箱 j=箱のカウンター
		j = 0;
		while (j < 20) { // 右下になるまでは探索する
			if (box[j] > 0 && box[j] < board_size * board_size) {
				if (board[box[j]] == id) { // 自分の石が置かれていたら
					pos = box[j];
					// 縦
					if ((pos + board_size) < (board_size * board_size)) { // ボードサイズ超えないか
						if (board[pos + board_size] == id) { // 連続縦2自コマか？
							// 上に置く場合ボード超えないか
							if ((pos - board_size) >= 0) {
								if (board[pos - board_size] == -1) { // 空白か
									if (put4(pos - board_size) == 1) {
										box[turns - 1] = pos - board_size;
										return ((pos - board_size)); // 三目置く
									}
								}
							}
							// 下に置く場合ボード超
							if (pos + (board_size + board_size) < (board_size * board_size)) {
								if (board[pos + (board_size + board_size)] == -1) { // 空白か
									if (put4(pos + (board_size + board_size)) == 1) {
										box[turns - 1] = pos + (board_size + board_size);
										return ((pos + (board_size + board_size))); // 三目置く
									}
								}
							}
						}
					}
					if (pos + (2 * board_size) < board_size * board_size) { // 間置く ボ超
						if (board[pos + (2 * board_size)] == id) { // 自コマか
							if (board[pos + board_size] == -1) { // 空白か
								if (put4(pos + board_size) == 1) {
									box[turns - 1] = pos + board_size;
									return (pos + board_size); // 三目置く
								}
							}
						}
					}
				}

				// 横
				if ((pos + 1) < board_size * board_size) { // ボ超
					if (board[pos + 1] == id && (pos + 1) % board_size != (board_size - 1)) { // 空白&飛石阻止
						// 左置く時，ボ超
						if (pos - 1 >= 0) {
							if (board[pos - 1] == -1 && (pos - 1) % board_size != (board_size - 1)) { // 空白&飛石阻止
								if (put4(pos - 1) == 1) {
									box[turns - 1] = pos - 1;
									return (pos - 1); // 三目置く
								}
							}
						}
						// 右置く時，ボ超
						if (pos + 2 < board_size * board_size) {
							if (board[pos + 2] == -1 && (pos + 2) % board_size != 0) { // 空白&飛石阻止
								if (put4(pos + 2) == 1) {
									box[turns - 1] = pos + 2;
									return (pos + 2); // 三目置く
								}
							}
						}
					}
					if (pos + 2 < board_size * board_size && (pos + 2) % board_size != 0) { // 間置く 飛石 ボ超
						if (board[pos + 2] == id) { // 自コマか
							if (board[pos + 1] == -1) { // 空白か
								if (put4(pos + 1) == 1) {
									box[turns - 1] = pos + 1;
									return (pos + 1); // 三目置く
								}
							}
						}
					}

					// 右斜め下\
					if (pos + board_size + 1 < board_size * board_size && (pos + board_size + 1) % board_size != 0) { // ボ超&飛石阻止
						if (board[pos + board_size + 1] == id) { // 自コマか

							// ボ超&飛石阻止 右下
							if (pos + (2 * (board_size + 1)) < (board_size * board_size)
									&& (pos + 2 * ((board_size + 1))) % board_size != 0) {
								if (board[pos + (2 * (board_size + 1))] == -1) { // 右下空白か
									if ((pos + (2 * (board_size + 1)) + 1) % board_size != 0
											&& (pos + (2 * (board_size + 1)) + 1) < board_size * board_size) { // 右隣マス飛石&ボ超
										if (board[pos + (2 * (board_size + 1)) + 1] != -1) { // 右隣マス置石
											if (put4(pos + (2 * (board_size + 1))) == 1) {
												box[turns - 1] = pos + (2 * (board_size + 1));
												return (pos + (2 * (board_size + 1))); // 三目置く
											}
										}
									}
									if ((pos + (2 * (board_size + 1)) + board_size) < board_size * board_size) { // 下マスボ超
										if (board[pos + (2 * (board_size + 1)) + board_size] != -1) { // 下マス置石
											if (put4(pos + (2 * (board_size + 1))) == 1) {
												box[turns - 1] = pos + (2 * (board_size + 1));
												return (pos + (2 * (board_size + 1))); // 三目置く
											}
										}
									}
									if (board[pos + (2 * (board_size + 1)) - board_size] != -1
											|| board[pos + (2 * (board_size + 1)) - 1] != -1) { // 置石判定
										if (put4(pos + (2 * (board_size + 1))) == 1) {
											box[turns - 1] = pos + (2 * (board_size + 1));
											return (pos + (2 * (board_size + 1))); // 三目置く
										}
									}
								}
							}

							// ボ超&飛石阻止 左上
							if (pos - (board_size + 1) > -1 && (pos - (board_size + 1)) % board_size != (board_size - 1)) {
								if (board[pos - (board_size + 1)] == -1) { // 右下空白か
									if ((pos - (board_size + 1) - 1) % board_size != (board_size - 1)
											&& (pos - (board_size + 1) - 1) >= 0) { // 左隣 飛石&ボ超
										if (board[pos - (board_size + 1) - 1] != -1) { // 左隣 置石
											if (put4(pos - (board_size + 1)) == 1) {
												box[turns - 1] = pos - (board_size + 1);
												return (pos - (board_size + 1)); // 三目置く
											}
										}
									}
									if ((pos - (board_size + 1) - board_size) >= 0) { // 上 ボ超
										if (board[pos - (board_size + 1) - board_size] != -1) { // 上 置石
											if (put4(pos - (board_size + 1)) == 1) {
												box[turns - 1] = pos - (board_size + 1);
												return (pos - (board_size + 1)); // 三目置く
											}
										}
									}
									if (board[pos - (board_size + 1) + board_size] != -1
											|| board[pos - (board_size + 1) + 1] != -1) { // 置石判定
										if (put4(pos - (board_size + 1)) == 1) {
											box[turns - 1] = pos - (board_size + 1);
											return (pos - (board_size + 1)); // 三目置く
										}
									}
								}
							}
						}
					}
					if (pos + 2 * (board_size + 1) < board_size * board_size && (pos + 2 * (board_size + 1)) % board_size != 0)

					{ // 間置くボ超飛石
						if (board[pos + 2 * (board_size + 1)] == id) { // 自コマか
							if (board[pos + board_size + 1] == -1) { // 空白か
								if (put4(pos + board_size + 1) == 1) {
									box[turns - 1] = pos + board_size + 1;
									return (pos + board_size + 1); // 三目置く
								}
								return (pos + board_size + 1);

							}
						}
					}
					// 左斜め下 /
					if ((pos + board_size - 1) < board_size * board_size
							&& (pos + board_size - 1) % board_size != (board_size - 1)) { // ボ超&飛石阻止
						if (board[pos + board_size - 1] == id) { // 自コマか

							// ボ超&飛石阻止 左下
							if (pos + (2 * (board_size - 1)) < board_size * board_size
									&& (pos + 2 * ((board_size - 1))) % board_size != (board_size - 1)) {
								if (board[pos + (2 * (board_size - 1))] == -1) { // 左下空白か
									if ((pos + (2 * (board_size - 1)) - 1) % board_size != (board_size - 1)) { // 左隣マス飛石
										if (board[pos + (2 * (board_size - 1)) - 1] != -1) { // 左隣マス置石
											if (put4(pos + (2 * (board_size - 1))) == 1) {
												box[turns - 1] = pos + (2 * (board_size - 1));
												return (pos + (2 * (board_size - 1))); // 三目置く
											}

										}
									}
									if ((pos + (2 * (board_size - 1)) + board_size) < board_size * board_size) { // 下マスボ超
										if (board[pos + (2 * (board_size - 1)) + board_size] != -1) { // 下マス置石
											if (put4(pos + (2 * (board_size - 1))) == 1) {
												box[turns - 1] = pos + (2 * (board_size - 1));
												return (pos + (2 * (board_size - 1))); // 三目置く

											}
										}
									}
									if (board[pos + (2 * (board_size - 1)) - board_size] != -1
											|| board[pos + (2 * (board_size - 1)) + 1] != -1) { // 置石判定
										if (put4(pos + (2 * (board_size - 1))) == 1) {
											box[turns - 1] = pos + (2 * (board_size - 1));
											return (pos + (2 * (board_size - 1))); // 三目置く

										}
									}
								}
							}

							// ボ超&飛石阻止 右上
							if (pos - (board_size - 1) > -1 && (pos - (board_size - 1)) % board_size != 0) {
								if (board[pos - (board_size - 1)] == -1) { // 右上空白か
									if ((pos - (board_size - 1) + 1) % board_size != 0) { // 右隣 飛石
										if (board[pos - (board_size - 1) + 1] != -1) { // 右隣 置石
											if (put4(pos - (board_size - 1)) == 1) {
												box[turns - 1] = pos - (board_size - 1);
												return (pos - (board_size - 1)); // 三目置く

											}
										}
									}
									if ((pos - (board_size - 1) - board_size) >= 0) { // 上 ボ超
										if (board[pos - (board_size - 1) - board_size] != -1) { // 上 置石
											if (put4(pos - (board_size - 1)) == 1) {
												box[turns - 1] = pos - (board_size - 1);
												return (pos - (board_size - 1)); // 三目置く

											}

										}
									}
									if (board[pos - (board_size - 1) + board_size] != -1
											|| board[pos - (board_size - 1) - 1] != -1) { // 置石判定
										if (put4(pos - (board_size - 1)) == 1) {
											box[turns - 1] = pos - (board_size - 1);
											return (pos - (board_size - 1)); // 三目置く

										}
									}
								}
							}
						}
					}
					if (pos + 2 * (board_size - 1) < board_size * board_size
							&& (pos + 2 * (board_size - 1)) % board_size != (board_size - 1)) { // 間置く ボ超&飛石
						if (board[pos + 2 * (board_size - 1)] == id) { // 自コマ
							if (board[pos + board_size - 1] == -1) { // 空白か
								if (put4(pos + board_size - 1) == 1) {
									box[turns - 1] = pos + (board_size - 1);
									return (pos + (board_size - 1)); // 三目置く

								}
							}
						}
					}
				}

			}
			j++; // 場所を１つ進める
		}

		// 二目 3目狙い2つおける
		j = 0;
		while (j < 20) { // 右下になるまでは探索する
			if (box[j] >= 0 && box[j] < board_size * board_size) {
				if (board[box[j]] == id) { // 自分の石が置かれていたら
					pos = box[j];

					if ((pos + 1) < board_size * board_size && (pos + 1) % board_size != 0) { // 右
						if ((pos + 2) < board_size * board_size && (pos + 2) % board_size != 0) { // 三目狙い1つめ
							if ((pos - 1) > -1 && (pos - 1) % board_size != (board_size - 1)) { // 三目狙い2つめ
								if (board[pos + 1] == -1 && board[pos + 2] == -1 && board[pos - 1] == -1) { // 空白
									if (put4(pos + 1) == 1) {
										box[turns - 1] = pos + 1;
										return (pos + 1); // 三目置く

									}
								}
							}
						}
					}

					if ((pos - 1) > -1 && (pos - 1) % board_size != (board_size - 1)) { // 左
						if ((pos - 2) > -1 && (pos - 2) % board_size != (board_size - 1)) { // 三目狙い1つめ
							if ((pos + 1) < board_size * board_size && (pos + 1) % board_size != 0) { // 三目狙い2つめ
								if (board[pos - 1] == -1 && board[pos - 2] == -1 && board[pos + 1] == -1) { // 空白
									if (put4(pos - 1) == 1) {
										box[turns - 1] = pos - 1;
										return (pos - 1); // 三目置く
									}
								}
							}
						}
					}

					if ((pos + board_size) < board_size * board_size) { // 下
						if ((pos + (2 * board_size)) < board_size * board_size) { // 三目狙い1つめ
							if ((pos - board_size) > -1) { // 三目狙い2つめ
								if (board[pos + board_size] == -1 && board[pos + (2 * board_size)] == -1
										&& board[pos - board_size] == -1) { // 空白
									if (put4(pos + board_size) == 1) {
										box[turns - 1] = pos + board_size;
										return (pos + board_size); // 三目置く
									}
								}
							}
						}
					}

					if ((pos - board_size) > -1) { // 上
						if (pos + board_size < board_size * board_size) { // 三目狙い1つめ
							if (pos - (2 * board_size) > -1) { // 三目狙い2つめ
								if (board[pos - board_size] == -1 && board[pos + board_size] == -1
										&& board[pos - (2 * board_size)] == -1) { // 空白
									if (put4(pos - board_size) == 1) {
										box[turns - 1] = pos - board_size;
										return (pos - board_size); // 三目置く
									}
								}
							}
						}
					}
				}
			}
			j++; // 場所を１つ進める
		}

		// 二目 3目狙い1つおける
		j = 0;
		while (j < 20) { // 右下になるまでは探索する
			if (box[j] >= 0 && box[j] < board_size * board_size) {
				if (board[box[j]] == id) { // 自分の石が置かれていたら
					pos = box[j];

					if ((pos + 1) < board_size * board_size && (pos + 1) % board_size != 0) { // 右
						if (((pos + 2) < board_size * board_size && (pos + 2) % board_size != 0) ||
								((pos - 1) > -1 && (pos - 1) % board_size != (board_size - 1))) { // 三目狙いどちらか1つ
							if (board[pos + 1] == -1 && (board[pos + 2] == -1 || board[pos - 1] == -1)) { // 空白
								if (put4(pos + 1) == 1) {
									box[turns - 1] = pos + 1;
									return (pos + 1); // 三目置く

								}
							}
						}
					}

					if ((pos - 1) > -1 && (pos - 1) % board_size != (board_size - 1)) { // 左
						if (((pos - 2) > -1 && (pos - 2) % board_size != (board_size - 1)) ||
								((pos + 1) < board_size * board_size && (pos + 1) % board_size != 0)) { // 三目狙いどちらか1つ
							if (board[pos - 1] == -1 && (board[pos - 2] == -1 || board[pos + 1] == -1)) { // 空白
								if (put4(pos - 1) == 1) {
									box[turns - 1] = pos - 1;
									return (pos - 1); // 三目置く
								}
							}
						}
					}

					if ((pos + board_size) < board_size * board_size) { // 下
						if (((pos + (2 * board_size)) < board_size * board_size) ||
								((pos - board_size) > -1)) { // 三目狙いどちらか一つ
							if (board[pos + board_size] == -1 && (board[pos + (2 * board_size)] == -1
									|| board[pos - board_size] == -1)) { // 空白
								if (put4(pos + board_size) == 1) {
									box[turns - 1] = pos + board_size;
									return (pos + board_size); // 三目置く
								}
							}
						}
					}

					if ((pos - board_size) > -1) { // 上
						if ((pos + board_size < board_size * board_size) &&
								(pos - (2 * board_size) > -1)) { // 三目狙いどちらか1つ
							if (board[pos - board_size] == -1 && (board[pos + board_size] == -1
									|| board[pos - (2 * board_size)] == -1)) { // 空白
								if (put4(pos - board_size) == 1) {
									box[turns - 1] = pos - board_size;
									return (pos - board_size); // 三目置く
								}
							}
						}
					}
				}
			}
			j++; // 場所を１つ進める
		}

		// 全て回避した場合

		int rad = random.nextInt(board_size * board_size); // randomをradに入れる
		box[turns - 1] = rad;
		return (rad); // ランダムに置く
	}

	/**
	 * 4マス判定
	 *
	 * @param pos 現在の座標
	 * @return 検証の座標（置けない場合は-1）
	 */
	private int put4(int nowPos) {
		int preScore = server.get_current_score()[id];
		int[] tryalBoard = server.get_current_board(); // ボードの状態を取得
		tryalBoard[nowPos] = id;
		if (server.evaluate_board(tryalBoard)[id] >= preScore) { // 試し置きが現在の得点を上回ったら
			return (1);
		} else {
			return (-1);
		}
	}

}
