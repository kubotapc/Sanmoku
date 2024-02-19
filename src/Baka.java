import java.util.Random;

public class Baka  extends Player {
	Random random = new Random();
	int pos;
	boolean firstPlay = false;

	public Baka() {
		player_name = "右に伸ばしたがり";
	}
	@Override
	void newgame(int firstplayer) {
		if ( id == firstplayer ) {
			firstPlay = true;	//初手が打てる場合は初手フラグを立てる
		} else {
			firstPlay = false;	//それ以外は初手フラグを下ろす
		}
	}

	@Override
	public int turn(boolean again) {
		if ( firstPlay == true ) {
			firstPlay = false;	//初手フラグを下ろす
			return ( board_size/2 * board_size );	//初手だったら、左端の中段に打つ
		}
		int[] board = server.get_current_board();	//ボードの状態を取得
		if ( !again ) {	//打ち直しでなければ
			pos = 0;	//探索する場所を左上に設定する
		}
		while ( pos < board_size * board_size-1 ) {		//右下になるまでは探索する
			if ( board[pos] == id ) {	//自分の石が置かれていたら
				pos++;		//右に1つ進める
				return (pos);	//進めた場所に石を置く（右隣に置く）
			} else {
				pos++;	//場所を１つ進める
			}
		}
		return random.nextInt(board_size * board_size); //右下まで来てしまったらランダムで置く
	}
}
