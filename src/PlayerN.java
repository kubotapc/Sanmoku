
public abstract class PlayerN extends Player implements UserClass {
	public int boardSize;
	public String playerName;

	public int[] getCurrentScore() {
		return ( server.get_current_score() );
	}
	public int[] getGameScore() {
		return ( server.get_game_score() );
	}
	public int[] getCurrentBoard() {
		return ( server.get_current_board() );
	}
	public String[] getPlayerNames() {
		return ( server.get_player_names() );
	}
	public int waitJikkyoClick() {
		return ( server.wait_jikkyo_click() );
	}
	public int[] evaluateBoard( int[] bd ) {
		return ( server.evaluate_board( bd ) );
	}
	public void dumpBoard( int[] bd ) {
		server.dump_board( bd );
	}
	void newGame( int firstPlayer ) {
	}

	@Override
	void newgame( int firstPlayer ) {
		boardSize = board_size;
		playerName = player_name;
		newGame( firstPlayer );
	}

	public abstract int turn(boolean again);

}
