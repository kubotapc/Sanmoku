public class Hashikara extends Player {
	int pos;
	
	@Override
	void newgame(int firstplayer) {
		pos = 0;
	}

	@Override
	public int turn(boolean again) {
		int hand = pos % (board_size * board_size); 
		pos++;
		//int[] hoge = server.get_current_board();
		//System.out.println(hoge);
		return  hand;
	}

}
