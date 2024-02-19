
public abstract class Player implements UserClass {
	int board_size;
	int id;
	int nplayer;
	int nstone;
	int ngame;
	Server server;
	String player_name;
	UserConsole myConsole; 

	String init(int id, int nplayer, int nstone, int ngame, int board_size,
			Server server) {
		this.id = id;
		this.nplayer = nplayer;
		this.nstone = nstone;
		this.ngame = ngame;
		this.board_size = board_size;
		this.server = server;
		if(player_name == null){
			player_name = this.getClass().getName();
		}
		System.out.format("%d %d %d %d %d%n", id, nplayer, nstone, ngame, board_size);
		myConsole = new UserConsole(id,player_name,server);
		myConsole.setVisible(false);

		return player_name;
	}

	@Override
	public String toString() {
		return player_name + ":(" +  String.valueOf((char)('A'+id))+")";
	}


	void newgame(int firstplayer){}

	public abstract int turn(boolean again);

	boolean isHuman(){
		return false;
	}

	void consoleOut( Object text ) {
		if ( server.enable_userConsole ) {
			myConsole.addText(text.toString());
			myConsole.setVisible(true);
		} else {
			myConsole.setVisible(false);
		}
	}
	void consoleOutln( Object text ) {
		consoleOut(text.toString()+"\n");
	}
}
