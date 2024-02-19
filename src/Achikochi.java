import java.util.Random;

public class Achikochi extends Player {
	Random random = new Random();
	int nturn;
	
	// コンストラクタは引数なしで。
	Achikochi(){
		// プレーヤ名はデフォールトではクラス名が使われるが、独自に設定したい場合はこんな感じ
		player_name = "Random Player";
	}

	@Override
	void newgame(int firstplayer) {
		nturn = 0;
	}

	@Override
	public int turn(boolean again) {
		if(! again) nturn++;
		return random.nextInt(board_size * board_size);
	}

}
