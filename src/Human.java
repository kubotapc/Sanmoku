
public class Human extends Player {
	String realname = "Human(手動入力プレーヤー)";

	Human(String realname) {
		this.realname = realname;
	}

	Human() {
		super();
	}

	@Override
	String init(int id, int nplayer, int nstone, int ngame, int board_size,
			Server server) {
		super.init(id, nplayer, nstone, ngame, board_size, server);
		return realname;
	}

	@Override
	public String toString() {
		return realname;
	}

	@Override
	public int turn(boolean again) {
		return server.wait_jikkyo_click();
	}

	@Override
	boolean isHuman() {
		return true;
	}
}
