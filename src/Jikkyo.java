import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Jikkyo extends JPanel implements MouseListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	Server server;
	int nplayer;
	int board_size;
	String[] player_names;

	JFrame frame;
	Color[] colors;
	static final int WIDTH = 1024;
	static final int HEIGHT = 768;
	static final int WAIT = 500; // msec

	int turn_id;
	int turn_no;

	Jikkyo(Server server, int nplayer, int board_size,
			String[] player_names) {
		this.server = server;
		this.nplayer = nplayer;
		this.board_size = board_size;
		this.player_names = player_names;

		init_sizes();

		frame = new JFrame("SANMOKU");
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.WHITE);
		Font font = new Font("SansSerif", Font.BOLD, 20);
		setFont(font);

		Container cp = frame.getContentPane();
		cp.add(this);

		addMouseListener(this);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.validate();
		frame.setVisible(true);

		colors = new Color[nplayer + 1]; // guard against "nplayer == 1"
		colors[0] = Color.black;
		colors[1] = Color.LIGHT_GRAY;
		float v1 = 1.0f;
		float f = 0.8f;
		float v2 = 1.0f;
		int v2i = 0;
		for (int i = 0; i < nplayer - 2; i++) {
			v1 = (f - v1) + f;
			v2i++;
			if (v2i == 2) {
				v2i = 0;
				v2 = (f - v2) + f;
			}
			colors[i + 2] = Color.getHSBColor((float) i / (float) (nplayer - 2), v1, v2);
		}
	}

	int[] board;
	int[] current_score;
	int[] game_score;

	private void draw_panel() {
		board = server.get_current_board();
		current_score = server.get_current_score();
		game_score = server.get_game_score();
		repaint();
		try {
			Thread.sleep(WAIT);
		} catch (InterruptedException e) {
		}
	}

	void paint_stone(Graphics2D g2, int v, int x, int y) {
		g2.setColor(colors[v]);
		g2.fillOval(x, y, radius, radius);
		g2.setColor(Color.WHITE);
		char[] mark = Character.toChars(0x41 + v);
		g2.drawChars(mark, 0, 1, x + (radius / 4), y + (radius * 3 / 4));
	}

	int margin;
	int delta;
	int length;
	int radius;

	private void init_sizes() {
		margin = 30;
		length = HEIGHT - margin - margin;
		delta = length / board_size;
		length = length - (length % delta);
		radius = (int) (delta * 0.8);
	}

	@Override
	public void paint(Graphics g) {
		if (board == null)
			return;
		Graphics2D g2 = (Graphics2D) g;
		super.paint(g);

		g2.drawString("stones left: " + Integer.toString(turn_no), margin, margin - 5);

		for (int i = 0; i <= board_size; i++) {
			g2.drawLine(i * delta + margin, margin, i * delta + margin, length + margin);
			g2.drawLine(margin, i * delta + margin, length + margin, i * delta + margin);
		}

		for (int y = 0; y < board_size; y++) {
			for (int x = 0; x < board_size; x++) {
				int i = y * board_size + x;
				int v = board[i];
				if (v != -1) {
					paint_stone(g2, v, x * delta + margin + (delta / 2) - (radius / 2),
							y * delta + margin + (delta / 2) - (radius / 2));
				}
			}
		}

		int dy = (HEIGHT - margin - margin) / nplayer;
		int x = HEIGHT - (margin / 2);
		for (int i = 0; i < nplayer; i++) {
			int y = i * dy + margin;
			g2.setColor(colors[i]);
			if (i == start_player) {
				g2.fillRect(x - 10, y, 8, radius);
			}
			g2.setColor(colors[i]);
			paint_stone(g2, i, x, y);
			g2.setColor(Color.BLACK);
			// y += (dy/3);
			// g2.drawString(player_names[i], x + radius + margin/4, y);
			// y += (dy/2);
			// String scores = current_score[i] + " / " + game_score[i] + "  " + player_names[i];
			if (server.player_disabled[i]) {
				g2.setColor(Color.lightGray);
			} else {
				if (i == turn_id) {
					g2.setColor(Color.red);
				} else {
					g2.setColor(Color.black);
				}
			}
			// String scores = String.format("%d / %2d %s", current_score[i], game_score[i], player_names[i]);
			String scores = String.format("%d / %2d", current_score[i], game_score[i]);
			// g2.drawString(player_names[i], x + radius + margin / 4, y + (dy / 2));
			// g2.drawString(scores, x + radius + margin , y + (dy / 2)+20);
			g2.drawString(player_names[i], x + radius + margin / 4, y + (radius / 2));
			g2.drawString(scores, x + radius + margin , y + (radius / 2)+20);
		}
	}

	/*
	int x = current_pos % board_size;
	int y = current_pos / board_size;
	try{
		for(int i = 0; i < 3; i++){
			Thread.sleep(500);
			g2.setColor(g2.getBackground());
			g2.fillOval(x * delta + margin + (delta / 2) - (radius / 2),
					y * delta + margin + (delta / 2) - (radius / 2),
					radius, radius);
			Thread.sleep(500);
			g2.setColor(Color.black);
			g2.fillOval(x * delta + margin + (delta / 2) - (radius / 2),
					y * delta + margin + (delta / 2) - (radius / 2),
					radius, radius);
		}
	} catch(InterruptedException e){
	}
	*/

	void endgame() {
		JOptionPane.showMessageDialog(this, "game end"
					+makeWinnersList(server.get_current_score()));
	}

	void endmatch() {
		draw_panel();
		JOptionPane.showMessageDialog(this, "match end"
					+makeWinnersList(server.get_game_score()));
		frame.dispose();
	}
	
	String makeWinnersList(int[] scores ) {
		int maxScore = 0;
		for ( int score : scores ) {
			if ( score > maxScore ) {
				maxScore = score;
			}
		}
//		ArrayList<String> winners = new ArrayList<>();
//		for ( int i=0 ; i<scores.length ; i++ ) {
//			if ( scores[i] == maxScore ) {
//				winners.add( player_names[i]);
//			}
//		}
		StringBuffer winners = new StringBuffer("\nwinnres:\n ");
		for ( int i=0 ; i<scores.length ; i++ ) {
			if ( scores[i] == maxScore ) {
				winners.append(String.valueOf((char)('A'+i))+":");
				winners.append(player_names[i]);
				winners.append("\n ");
			}
		}
		return ( winners.toString() );
	}
	
	int start_player;

	void newgame(int start_player) {
		this.start_player = start_player;
		draw_panel();
	}

	int current_pos;

	void play_stone(int id, int pos) {
		current_pos = pos;
		draw_panel();
	}

	void turn_start(int id, int s) {
		// System.out.format("turn_start: %d%n", id);
		turn_id = id;
		turn_no = s;
		repaint();
	}

	boolean waiting;
	int click_location;

	void signal_click(int loc) {
		if (waiting) {
			synchronized (this) {
				click_location = loc;
				notify();
				waiting = false;
			}
		}
	}

	int wait_click() {
		int result = 0;
		synchronized (this) {
			waiting = true;
			while (waiting) {
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				result = click_location;
			}
		}
		return result;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if ((x >= margin) && (x <= margin + length) && (y >= margin) && (y <= margin + length)) {
			int ix = (y - margin) / delta * board_size + (x - margin) / delta;
			System.out.format("mouseClicked %d %d %d%n", x, y, ix);
			signal_click(ix);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
