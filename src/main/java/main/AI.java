package main;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class AI {
	class Move{//Object for a move for the machine learning AI's, has a board, 
		//next move and value for that next move associated to each one
		int value;
		int nextMove;
		String currBoard;
	}

	@Autowired
	Server server = new Server();//Linking server, so can access methods in Server
	
	String URI = "file:/Users/Alex%20Vanlint/Documents/GitHub/A-Level-Project/3x3Moves.txt";
	Random random = new Random();
	int learning3wins = 0, oppAIwins = 0, draws = 0;
	List<Move> moves = new ArrayList<Move>();
	List<Integer> allyMoves = new ArrayList<Integer>();
	List<Integer> opponentMoves = new ArrayList<Integer>();
	String oldBoard = server.setUpBoards(3);	

	//-----------------------------------------
	void setUpLearning3(){//Imports all the current knowledge from the text file, also resets any variables from before
		learning3wins = 0; oppAIwins= 0; draws = 0;
		moves.clear();
		String[] temp;
		try{
			String[] outputs = server.readFileNotResource(URI).split(System.lineSeparator());
			//An array of the lines within the file
			for (int i = 0; i < outputs.length; i ++){
				if (!outputs[i].isEmpty()) {
					temp = outputs[i].split(" ");//an array of parts of one line (splitting up the board, next move and value)
					moves.add(new Move());
					moves.get(i).value = Integer.parseInt(temp[2]);
					moves.get(i).nextMove = Integer.parseInt(temp[1]);
					moves.get(i).currBoard = temp[0];
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	//---------------------------------------------
	void addMoves(int newPosition){//Adds all the valid moves available in oldBoard to the moves list
		List<Integer> validMoves = findValidMoves(oldBoard.toCharArray(), 0);
		for (int i = 0; i < validMoves.size(); i ++){
			moves.add(new Move());
			moves.get(newPosition + i).currBoard = oldBoard;
			moves.get(newPosition + i).nextMove = validMoves.get(i);
			moves.get(newPosition + i).value = 0;
		}
	}
	//--------------------------------------------------------
	String learning3(String board, char symbol){//main bit of machine learning AI
		checkIfMoveExists(board, false);//Records opponents moves
		checkIfMoveExists(board, false);//Adding moves that appear after opponents move 
		//(ones that can potentially be chosen by learning AI)
		List<Integer> availableMoves = new ArrayList<Integer>();
		for (int i = 0; i < moves.size(); i ++){//Adds all the available moves to the list
			if (moves.get(i).currBoard.equals(board)){
				availableMoves.add(i);
			}
		}
		int best = -1000;
		int currValue, position = 0;
		for(int i = 0; i < availableMoves.size(); i ++){//Finds the highest valued move (with the randomisation)
			currValue = moves.get(availableMoves.get(i)).value + random.nextInt(10);
			if (currValue > best){
				best = currValue;
				position = i;//Pointer for the move
			}
		}
		char[] tempBoard = board.toCharArray();
		tempBoard[moves.get(availableMoves.get(position)).nextMove] = symbol;//Changes the board
		board = new String(tempBoard);
		checkIfMoveExists(board, true);//Records ally move
		return board;
	}
	//--------------------------------------------------------------
	String testLearning3(int loop){//pits the learning AI against rule AI, thus making it learn
		setUpLearning3();
		char first;
		int learning = 0, opp = 0;
		String board;
		for (int i = 0; i < loop; i ++){//Does a loop for each game
			board = server.setUpBoards(3);
			oldBoard = board;
			if (random.nextInt(2) == 0){//randomly chooses who goes first each game
				first = 'l';
				learning ++;
			}
			else{
				first = 'o';
				opp++;
			}
			allyMoves.clear();
			opponentMoves.clear();
			while (true){//Actually plays the game here until it ends
				if (first == 'l'){
					board = learning3(board, 'X');
					if (checkIfGameOver(board, 'X')){//Also changes values of moves at end of game
						break;
					}
					board = rule3('O', board);
					if (checkIfGameOver(board, 'X')){
						break;
					}
				}
				else{
					board = rule3('X', board);
					if (checkIfGameOver(board, 'O')){
						break;
					}
					board = learning3(board, 'O');
					if (checkIfGameOver(board, 'O')){
						break;
					}
				}
			}
		}
		writeFile(URI);//Writes knowledge after all the games into text file
		return "Times that learning AI won: " + learning3wins + "<br>Times that rule AI won: " + oppAIwins + "<br>"
		+ "Draws: " + draws + "<br>Total games: " + (learning3wins + oppAIwins + draws)
		+ "<br>Times learning AI went first: " + learning + "<br>Times rule AI went first: " + opp;//returns statistics to web page
	}
	//-----------------------------------------------------------
	boolean checkIfGameOver(String board, char learning){
		List<Integer> winner, loser;
		if (isCaptured(board, 'X')){//Did X win?
			if (learning == 'O'){//Were learning O's and hence lost?
				loser = allyMoves;
				winner = opponentMoves;
				oppAIwins ++;
			}
			else{
				loser = opponentMoves;
				winner = allyMoves;
				learning3wins ++;
			}
			for (int i = 0; i < winner.size(); i ++){
				moves.get(winner.get(i)).value ++;//Increases all the values of the moves made by the winner (learning)
			}
			for (int i = 0; i < loser.size(); i ++){
				moves.get(loser.get(i)).value --;//Decreases all the values of the moves made by the loser (learning)
			}
			return true;//X's won
		}
		else if (isCaptured(board, 'O')){//Did O's win?
			if (learning == 'O'){//Were learning O's and hence win?
				loser = opponentMoves;
				winner = allyMoves;
				learning3wins ++;
			}
			else{
				loser = allyMoves;
				winner = opponentMoves;
				oppAIwins ++;
			}
			for (int i = 0; i < winner.size(); i ++){
				moves.get(winner.get(i)).value ++;//Increases all the values of the moves made by the winner (learning)
			}
			for (int i = 0; i < loser.size(); i ++){
				moves.get(loser.get(i)).value --;//Decreases all the values of the moves made by the loser (learning)
			}
			return true;//O's won
		}
		for (int i = 0; i < board.length(); i ++){
			if (board.charAt(i) == '_'){
				return false;//can still play
			}
		}//else draw
		draws ++;
//		for (int i = 0; i < opponentMoves.size(); i ++){
//			moves.get(opponentMoves.get(i)).value ++;//Changes the values of all the values.
//			//Increases values if encouraging AI to draw, decreases if discouraging AI to draw
//		}
//		for (int i = 0; i < allyMoves.size(); i ++){
//			moves.get(allyMoves.get(i)).value ++;
//		}
		return true;//draw
	}
	//------------------------------------------------
	void checkIfMoveExists(String board, boolean ally){
		int move = getDiff(board, oldBoard);
		boolean found = false;
		if (move != -1){//If there is a difference between the two boards
			for (int i = 0; i < moves.size(); i ++){//Finds the specific move that was played
				if (moves.get(i).currBoard.equals(oldBoard) && moves.get(i).nextMove == move){
					found = true;
					if (ally){
						allyMoves.add(i);//Adds the move to the respective list
					}
					else{
						opponentMoves.add(i);
					}
					break;
				}
			}
			if (found == false){//If move wasn't found in the moves list
				int newPositions = moves.size();
				addMoves(newPositions);//Adds all the valid moves for the oldBoard variable
				//including the move that was just played
				for (int i = newPositions; i < moves.size(); i ++){
					if (moves.get(i).nextMove == move){
						if (ally){//Then adds the move that was just played to the respective list
							allyMoves.add(i);
						}
						else{
							opponentMoves.add(i);
						}
					}
				}
			}
			oldBoard = board;//Updates the oldBoard variable
		}
		else{//If the boards are the same
			for (int i = 0; i < moves.size(); i ++){//Finds if the moves list has moves for the current board
				if (moves.get(i).currBoard.equals(board)){
					found = true;//If there are, then does nothing
				}
			}
			if (found == false){//If there aren't, then add all the valid moves for the oldBoard variable
				//(which is the same as the current board)
				addMoves(moves.size());
			}
		}
	}
	String testRule(int loop){//test to see how well the minimax AI did against the greedy AI (one that incorporated 3x3 rule AI)
		String board, tempBoard;
		boolean repeat = false;
		List<Integer> movesPlayed = new ArrayList<Integer>();
		int wins = 0, losses = 0, draws = 0, ruleFirst = 0;
		String first;
		for (int i = 0; i < loop; i ++) {//repeats for as many games specified
			board = server.setUpBoards(3);//resets variables every game
			tempBoard = board;
			if (random.nextInt(2) == 0){//randomly chooses who goes first
				first = "rule";
				ruleFirst ++;
			}
			else{
				first = "random";
			}
			while (true){
				if (first.equals("random")){
					tempBoard = random3(board, 'X');
					movesPlayed.add(getDiff(tempBoard, board));
					board = tempBoard;
					if (isCaptured(board, 'X')){
						losses ++;
						break;
					}
					if (isDraw(board)){
						draws ++;
						break;
					}
					tempBoard = rule3('O', board);
					movesPlayed.add(getDiff(tempBoard, board));
					board = tempBoard;
					if(isCaptured(board, 'O')){
						wins ++;
						break;
					}
					if (isDraw(board)){
						draws ++;
						break;
					}
				}
				else{
					tempBoard = rule3('X', board);
					movesPlayed.add(getDiff(tempBoard, board));
					board = tempBoard;
					if(isCaptured(board, 'X')){
						wins ++;
						break;
					}
					if (isDraw(board)){
						draws ++;
						break;
					}
					tempBoard = random3(board, 'O');
					movesPlayed.add(getDiff(tempBoard, board));
					board = tempBoard;
					if(isCaptured(board, 'O')){
						losses ++;
						break;
					}
					if (isDraw(board)){
						draws ++;
						break;
					}
				}
			}
			if (losses == 1 && repeat == false){
				repeat = true;
				System.out.println(first + ": " + movesPlayed);
			}
			movesPlayed.clear();
		}
		return "Total Games: " + (losses + wins + draws) + "<br>wins: " + wins + "<br>Times that ruleAI went first: " 
		+ ruleFirst + "<br>losses: " + losses + "<br>draws: " + draws;//returns stats to webpage
	}
	boolean isDraw(String board){
		for (int i = 0; i < board.length(); i ++){
			if (board.charAt(i) == '_'){
				return false;
			}
		}
		return true;
	}
	//--------------------------------------	
	String random3(String board1, char symbol){//random AI for 3x3 tic tac toe
		char[] board = board1.toCharArray();
		List<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < board.length; i ++){
			if (board[i] == '_'){//collects all the empty spaces into a list
				positions.add(i);
			}
		}
		board[positions.get(random.nextInt(positions.size()))] = symbol;//randomly chooses an empty space
		//and puts a symbol there
		return new String(board);
	}
	//---------------------------------------
	String rule3(char symbol, String board){//rule based AI for 3x3 tic tac toe
		char[] board2 = board.toCharArray();
		List<Integer> friendlyPositions = new ArrayList<Integer>();
		List<Integer> foePositions = new ArrayList<Integer>();
		for (int i = 0; i < board2.length; i ++){
			if (board2[i] == symbol){
				friendlyPositions.add(i);//stores the positions of all the AI's symbols
			}
		}
		List<Integer> won = find2InRow(friendlyPositions, board2);
		//Finds all the possible places it can make a 3 in a row
		for (int i = 0; i < board2.length; i ++){
			if (board2[i] != symbol && board2[i] != '_'){
				foePositions.add(i);//stores the positions of all the opponent's symbols
			}
		}
		List<Integer> stopOtherPlayer = find2InRow(foePositions, board2);//Finds all the places
		//the opponent can make a 3 in a row
		int move = getMove(symbol, board2);//finds the best move otherwise
		if (!won.isEmpty()){//Sees if it can win
			board2[won.get(random.nextInt(won.size()))] = symbol;
		}
		else if (!stopOtherPlayer.isEmpty()){//Sees if it can stop opponent from winning
			board2[stopOtherPlayer.get(random.nextInt(stopOtherPlayer.size()))] = symbol;
		}
		else if (checkCornerPosition(board.toCharArray(), symbol)) {//Checks if it this position
			// |X|_|_|
			// |_|O|_|
			// |_|_|X|
			//or the other rotation of it, where X is the opponent and O is the ally
			int[] numbers = {1,3,5,7};
			board2[numbers[random.nextInt(numbers.length)]] = symbol;//randomly plays in one of the side positions
		}
		else if (checkOtherPositions(board.toCharArray(), symbol) != -1){
			//checks other specific scenarios described in the function
			board2[checkOtherPositions(board.toCharArray(), symbol)] = symbol;
		}
		else {//else plays the best move otherwise
			if (move == -1){
				return server.getThreeBoard();
			}
			board2[move] = symbol;
		}
		server.setThreeBoard(new String(board2));
		return new String(board2);
	}
	//------------------------------------------
	int checkOtherPositions(char[] board, char ally){//checks the other specific positions that ruleAI loses
		//_|X|_ or _|X|_
		//_|O|_    X|O|_
		//X|_|_    _|_|_
		char opp;
		if (ally == 'X')
			opp = 'O';
		else
			opp = 'X';
		if (board[4] == ally){
			//first position
			if (board[1] == opp){//do this for the 4 rotations of the board
				if (board[6] == opp){
					if (board[0] == '_' && board[2] == '_' && board[3] == '_' && board[5] == '_' && board[7] == '_' && board[8] == '_'){
						//check if every other square is empty
						return 0;//returns the square that the AI should play in
					}
				}
				if (board[8] == opp){
					if (board[0] == '_' && board[2] == '_' && board[3] == '_' && board[5] == '_' && board[6] == '_' && board[7] == '_'){
						//check if every other square is empty
						return 2;//returns the square that the AI should play in
					}
				}
			}
			if (board[3] == opp){//second rotation
				if (board[2] == opp){
					if (board[0] == '_' && board[1] == '_' && board[5] == '_' && board[6] == '_' && board[7] == '_' && board[8] == '_'){
						//check if every other square is empty
						return 0;//returns the square that the AI should play in
					}
				}
				if (board[8] == opp){
					if (board[0] == '_' && board[1] == '_' && board[2] == '_' && board[5] == '_' && board[6] == '_' && board[7] == '_'){
						//check if every other square is empty
						return 6;//returns the square that the AI should play in
					}
				}
			}
			if (board[5] == opp){//third rotation
				if (board[0] == opp){
					if (board[1] == '_' && board[2] == '_' && board[3] == '_' && board[6] == '_' && board[7] == '_' && board[8] == '_'){
						//check if every other square is empty
						return 2;//returns the square that the AI should play in
					}
				}
				if (board[6] == opp){
					if (board[0] == '_' && board[1] == '_' && board[2] == '_' && board[3] == '_' && board[7] == '_' && board[8] == '_'){
						//check if every other square is empty
						return 8;//returns the square that the AI should play in
					}
				}
			}
			if (board[7] == opp){//fourth rotation
				if (board[0] == opp){
					if (board[1] == '_' && board[2] == '_' && board[3] == '_' && board[5] == '_' && board[6] == '_' && board[8] == '_'){
						//check if every other square is empty
						return 0;//returns the square that the AI should play in
					}
				}
				if (board[2] == opp){
					if (board[0] == '_' && board[1] == '_' && board[3] == '_' && board[5] == '_' && board[6] == '_' && board[8] == '_'){
						//check if every other square is empty
						return 8;//returns the square that the AI should play in
					}
				}
			}
			//second position
			if (board[1] == opp && board[3] == opp){//rotation 1
				if (board[0] == '_' && board[2] == '_' && board[5] == '_' && board[6] == '_' && board[7] == '_' && board[8] == '_'){
					//checking every other square is blank
					return 0;
				}
			}
			if (board[3] == opp && board[7] == opp){//rotation 2
				if (board[0] == '_' && board[1] == '_' && board[2] == '_' && board[5] == '_' && board[6] == '_' && board[8] == '_'){
					//checking every other square is blank
					return 6;
				}
			}
			if (board[5] == opp && board[7] == opp){//rotation 3
				if (board[0] == '_' && board[1] == '_' && board[2] == '_' && board[3] == '_' && board[6] == '_' && board[8] == '_'){
					//checking every other square is blank
					return 8;
				}
			}
			if (board[1] == opp && board[5] == opp){//rotation 4
				if (board[0] == '_' && board[2] == '_' && board[3] == '_' && board[6] == '_' && board[7] == '_' && board[8] == '_'){
					//checking every other square is blank
					return 2;
				}
			}
		}
		return -1;
	}
	//------------------------------------
	int interpretPosition(int position){//returns value given on how many lines the square is apart of
		if (position == 0 || position == 2 || position == 6 || position == 8){//corner square (apart of 3 lines)
			return 3;
		}
		else if (position == 4){//middle square (apart of 4 lines)
			return 4;
		}
		else{//only other squares are the edge squares (1, 3, 5, 7) and they are apart of 2 lines each
			return 2;
		}
	}
	//--------------------------------------------------------
	boolean checkCornerPosition(char[] board, char symbol) {
		char opp;
		if (symbol == 'X') {
			opp = 'O';
		}
		else {
			opp = 'X';
		}
		if (board[4] == symbol) {//checks if the centre is an AI symbol
			if (board[0] == opp && board[8] == opp) {//checks if the opposite corners are the opponent's symbols
				if (board[1] == '_' && board[2] == '_' && board[3] == '_' && board[5] == '_' && board[6] == '_' && board[7] == '_') {
					//checks if every other square is empty
					return true;
				}
			}
			if (board[2] == opp && board[6] == opp) {//other two opposite corners
				if (board[0] == '_' && board[1] == '_' && board[3] == '_' && board[5] == '_' && board[7] == '_' && board[8] == '_') {
					return true;
				}
			}
		}
		return false;
	}
	//------------------------------------------------------
	int getMove(char symbol, char[] board){//getting best move otherwise
		//a system which calculates for each square the number of winning possibilities
		int best = -1;
		int value;
		int position = -1;
		for (int i = 0; i < 9; i ++){
			if (board[i] == '_'){//cycles through every empty space
				value = findWinningPossibilities(i, symbol, board, true);//gets a value from this method
				if (value == best){
					if (interpretPosition(i) > interpretPosition(position)){
						best = value;
						position = i;
					}
				}
				else if (value > best){
					best = value;
					position = i;//Gets the best valued move
				}
			}
		}
		return position;
	}
	//------------------------------------------------------
	int findWinningPossibilities(int position, char ai, char[] board, boolean complex){
		//for a given position, board and ally symbol, returns a value
		char opp;
		int[] returnint;
		if (ai == 'X'){
			opp = 'O';
		}
		else{
			opp = 'X';
		}
		int no = 0;
		int x = 0;
		switch (position){
		case 0:
			returnint = repeatedCode(opp, no, x, 1, 2, board);//does this for each line that the position is apart of
			returnint = repeatedCode(opp, returnint[0], returnint[1], 3, 6, board);//line 0, 3, 6
			returnint = repeatedCode(opp, returnint[0], returnint[1], 4, 8, board);//line 0, 4, 8
			no = returnint[0];//totals up a number for both no and x after going through each line
			x = returnint[1];
			break;
		case 1:
			returnint = repeatedCode(opp, no, x, 4, 7, board);//line 1, 4, 7
			returnint = repeatedCode(opp, returnint[0], returnint[1], 0, 2, board);// line 1, 0, 2
			no = returnint[0];
			x = returnint[1];
			break;
		case 2:
			returnint = repeatedCode(opp, no, x, 0, 1, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 4, 6, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 5, 8, board);
			no = returnint[0];
			x = returnint[1];
			break;
		case 3:
			returnint = repeatedCode(opp, no, x, 0, 6, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 4, 5, board);
			no = returnint[0];
			x = returnint[1];
			break;
		case 4:
			returnint = repeatedCode(opp, no, x, 0, 8, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 1, 7, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 2, 6, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 3, 5, board);
			no = returnint[0];
			x = returnint[1];
			break;
		case 5:
			returnint = repeatedCode(opp, no, x, 3, 4, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 2, 8, board);
			no = returnint[0];
			x = returnint[1];
			break;
		case 6:
			returnint = repeatedCode(opp, no, x, 0, 3, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 7, 8, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 2, 4, board);
			no = returnint[0];
			x = returnint[1];
			break;
		case 7:
			returnint = repeatedCode(opp, no, x, 1, 4, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 6, 8, board);
			no = returnint[0];
			x = returnint[1];
			break;
		case 8:
			returnint = repeatedCode(opp, no, x, 0, 4, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 2, 5, board);
			returnint = repeatedCode(opp, returnint[0], returnint[1], 6, 7, board);
			no = returnint[0];
			x = returnint[1];
			break;
		}
		if (complex){//if parameter is true
			if (x >= 2){//if there are at least 2 lines where there is an ally symbol and a space
				no = 100;//this is high enough to guarantee being chosen over other positions
				//this will get 2 lines with 2 symbols in, therefore guaranteeing a win next turn
			}
			else if (x == 1){//if there is a line with another ally symbol in it, then add 1.5 to the value
				no += 1.5;
			}
		}
		else{//if parameter is false
			no += x;//this is basically giving a value equal to the number of lines that are winnable for this position
		}
		return no;//returns the value for that position
	}
	//-------------------------------------------------------
	int[] repeatedCode(char opp, int no, int x, int first, int second, char[] board){
		if (board[first] == '_' && board[second] == '_'){//Checks if the other two spaces in the line are empty
			no ++;
		}
		else if (board[first] != opp && board[second] != opp){//Checks if the other two spaces in the line are empty or
			//AI's symbol (so at least one of them is the AI symbol, since last if statement checked both being empty)
			x ++;
		}
		int[] returnint = {no, x};
		return returnint;//returns both numbers
	}
	//------------------------------------------------------
	List<Integer> find2InRow(List<Integer> positions, char[] board){//Checks the whole board and returns all the squares
		//which would make a 3 in a row with the positions given (a list)
		List<Integer> squares = new ArrayList<Integer>();
		if (positions.contains(0)){
			if (positions.contains(1)){
				if (board[2] == '_'){
					squares.add(2);
				}
			}
			else if (positions.contains(2)){
				if (board[1] == '_'){
					squares.add(1);
				}
			}
			if (positions.contains(4)){
				if (board[8] == '_'){
					squares.add(8);
				}
			}
			else if (positions.contains(8)){
				if (board[4] == '_'){
					squares.add(4);
				}
			}
			if (positions.contains(3)){
				if (board[6] == '_'){
					squares.add(6);
				}
			}
			else if (positions.contains(6)){
				if (board[3] == '_'){
					squares.add(3);
				}
			}
		}
		if (positions.contains(1)){
			if (positions.contains(4)){
				if (board[7] == '_'){
					squares.add(7);
				}
			}
			else if (positions.contains(7)){
				if (board[4] == '_'){
					squares.add(4);
				}
			}
			if (positions.contains(2)){
				if (board[0] == '_'){
					squares.add(0);
				}
			}
		}
		if (positions.contains(2)){
			if (positions.contains(4)){
				if (board[6] == '_'){
					squares.add(6);
				}
			}
			else if (positions.contains(6)){
				if (board[4] == '_'){
					squares.add(4);
				}
			}
			if (positions.contains(5)){
				if (board[8] == '_'){
					squares.add(8);
				}
			}
			else if (positions.contains(8)){
				if (board[5] == '_'){
					squares.add(5);
				}
			}
		}
		if (positions.contains(3)){
			if (positions.contains(6)){
				if (board[0] == '_'){
					squares.add(0);
				}
			}
			if(positions.contains(4)){
				if (board[5] == '_'){
					squares.add(5);
				}
			}
			else if (positions.contains(5)){
				if (board[4] == '_'){
					squares.add(4);
				}
			}
		}
		if (positions.contains(4)){
			if (positions.contains(8)){
				if (board[0] == '_'){
					squares.add(0);
				}
			}
			if (positions.contains(6)){
				if (board[2] == '_'){
					squares.add(2);
				}
			}
			if (positions.contains(7)){
				if (board[1] == '_'){
					squares.add(1);
				}
			}
			if (positions.contains(5)){
				if (board[3] == '_'){
					squares.add(3);
				}
			}
		}
		if (positions.contains(5)){
			if (positions.contains(8)){
				if (board[2] == '_'){
					squares.add(2);
				}
			}
		}
		if (positions.contains(6)){
			if (positions.contains(7)){
				if (board[8] == '_'){
					squares.add(8);
				}
			}
			else if (positions.contains(8)){
				if (board[7] == '_'){
					squares.add(7);
				}
			}
		}
		if (positions.contains(7)){
			if (positions.contains(8)){
				if (board[6] == '_'){
					squares.add(6);
				}
			}
		}
		return squares;
	}
	//---------------------------------------------------------------------
	int getDiff(String board1, String board2){//returns the first position where the two strings differ
		for (int i = 0; i < board1.length(); i ++){
			if (board1.charAt(i) != board2.charAt(i)){
				return i;
			}
		}
		return -1;
	}
	//--------------------------------------------------------------
	List<Integer> findValidMoves(char[] board, int square){//Returns all the positions of empty squares in a 3x3 board
		//For 3x3 tic tac toe, square will always be 0, but for 9x9, the board is the whole thing and the square is the
		//minisquare that is being looked at
		List<Integer> validMoves = new ArrayList<Integer>();
		if (square != -1){
		for (int i = 0; i < 9; i ++){
			if (board[(square * 9) + i] == '_'){
				validMoves.add((square * 9) + i);
			}
		}
		}
		else{
			for (int i = 0; i < board.length; i ++){
				if (board[i] == '_'){
					validMoves.add(i);
				}
			}
		}
		return validMoves;
	}
	//-------------------------------------------------------------
	boolean isCaptured(String board, char ally){//Checks if the board has been won by the specified character (X's or O's)
		if (ally == 'X' || ally == 'O'){
			if (board.charAt(0) == ally){
				if (board.charAt(1) == ally && board.charAt(2) == ally){
					return true;
				}
				if (board.charAt(4) == ally && board.charAt(8) == ally){
					return true;
				}
				if (board.charAt(3) == ally && board.charAt(6) == ally){
					return true;
				}
			}
			if (board.charAt(1) == ally && board.charAt(4) == ally && board.charAt(7) == ally){
				return true;
			}
			if (board.charAt(2) == ally){
				if (board.charAt(4) == ally && board.charAt(6) == ally){
					return true;
				}
				if (board.charAt(5) == ally && board.charAt(8) == ally){
					return true;
				}
			}
			if (board.charAt(3) == ally && board.charAt(4) == ally && board.charAt(5) == ally){
				return true;
			}
			if (board.charAt(6) == ally && board.charAt(7) == ally && board.charAt(8) == ally){
				return true;
			}
		}
		return false;
	}
	//------------------------------------------------------------------
	void writeFile(String URI){//Writes all the information to the file
		try {
			URI uri = new URI(URI);
			File file = new File(uri);
			Writer write = new FileWriter(file);
			write.write("");
			write.close();
			write = new FileWriter(file, true);
			for (int i = 0; i < moves.size(); i ++){
				write.write(moves.get(i).currBoard + " " + moves.get(i).nextMove + " " + moves.get(i).value + System.lineSeparator());
			}
			write.close();
		} catch (Exception e) {
			System.out.println("BAD");
			e.printStackTrace();
		}
	}
}
