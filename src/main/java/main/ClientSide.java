package main;

import org.springframework.stereotype.Component;

@Component
public class ClientSide {
	String create3Board(char[] board, int delay){//creates the html table for the 3x3 board
		String returnMessage = "<table id = \"table" + (delay/9) + "\">";//start of table
		String cellContents;
		for (int i = 1; i < 9; i += 3){
			if (i != 1){//if not the first row (adds a border above)
				returnMessage = returnMessage + "<tr class = \"BorderRow\">";
			}
			else{
				returnMessage = returnMessage + "<tr>";//first row (doesn't have border above)
			}
			for (int j = 0; j < 3; j ++){
				if (board[i + j - 1] == '_'){//if square is blank, adds a button there
					cellContents =  "<button type = \"button\"; "
							+ "class = \"button\"; "
							+ "id = \"button" + (i + j + delay) + "\"; "
							+ "onclick = \"changeCell(" + (i + j + delay) + ")\"></button>";
				}
				else{//if square not blank, then adds what was there before
					cellContents = board[i + j - 1] + "";
				}
				if (j != 0){ //for borders
					returnMessage = returnMessage + "<td "
							+ "id = \"" + (i + j + delay) + "\"; "
							+ "class = \"BorderLeft\">"
							+ cellContents
							+ "</td>";
				}
				else{ //for first column of cells, no border
					returnMessage = returnMessage + "<td "
							+ "id = \"" + (i + j + delay) + "\"; >"
							+ cellContents
							+ "</td>";
				}
			}
			returnMessage = returnMessage + "</tr>";//finishes row
		}
		return returnMessage + "</table>";//finishes table
	}
	//------------------------------------------------------------------------------------
	String create9Board(char[] board){
		char[] smallBoard = new char[9];
		String returnMessage = "<table class = \"table1\">";
		String cellContents;
		for (int i = 0; i < 9; i += 3){
			if (i != 0){//not the first row (has a border above) 
				returnMessage = returnMessage + "<tr class = \"thickBorderRow\">";
			}
			else{//first row (doesnt have a border above)
				returnMessage = returnMessage + "<tr class = \"noBorderRow\">";
			}
			for (int j = 0; j < 3; j ++){
				System.arraycopy(board, (9 * i + 9 * j), smallBoard, 0, 9);//copies the minisquare into smallBoard
				if (checkIfCaptured(smallBoard, 'X')){//if minisquare is captured by X
					cellContents = "X";//that minisquare will be displayed as an X
				}
				else if (checkIfCaptured(smallBoard, 'O')){//does same with O
					cellContents = "O";
				}
				else {//if minisquare not captured
					cellContents = create3Board(smallBoard, (9 * i + 9 * j));//make a 3x3 table inside minisquare
					//displays all the information (X, O, buttons for blanks)
				}
				if (j != 0){//not left most side (has a border to the left)
					returnMessage = returnMessage + "<th class = \"thickBorderLeft\"; "
							+ "id = \"cell" + (i + j) + "\">"
							+ cellContents
							+ "</th>";
				}
				else{//left most side (doesn't have a border to the left)
					returnMessage = returnMessage + "<th id = \"cell" + (i + j) + "\"; "
							+ "class = \"bigTableCell\">"
							+ cellContents
							+ "</th>";
				}
			}
			returnMessage = returnMessage + "</tr>";//finishes row
		}
		return returnMessage + "</table>";//finishes table
	}
	//-----------------------------------------------------------------------------------
	boolean checkIfCaptured(char[] board, char symbol){//checks if a minisquare has been captured by input symbol
		if (board[0] == symbol){
			if (board[1] == symbol && board[2] == symbol){//line 0, 1, 2
				return true;
			}
			else if (board[3] == symbol && board[6] == symbol){//line 0, 3, 6
				return true;
			}
			else if (board[4] == symbol && board[8] == symbol){//line 0, 4, 8
				return true;
			}
		}
		if (board[1] == symbol){
			if (board[4] == symbol && board[7] == symbol){//line 1, 4, 7
				return true;
			}
		}
		if (board[2] == symbol){
			if (board[4] == symbol && board[6] == symbol){//line 2, 4, 6
				return true;
			}
			else if (board[5] == symbol && board[8] == symbol){//line 2, 5, 8
				return true;
			}
		}
		if (board[3] == symbol){
			if (board[4] == symbol && board[5] == symbol){//line 3, 4, 5
				return true;
			}
		}
		if (board[6] == symbol){
			if (board[7] == symbol && board[8] == symbol){//line 6, 7, 8
				return true;
			}
		}
		return false;
	}
}
