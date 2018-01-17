package main;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.AI.Move;
@Component
public class AI9 {
	@Autowired
	AI ai = new AI();
	@Autowired
	Server server = new Server();

	String URI = "file:/Users/Alex%20Vanlint/Documents/GitHub/A-Level-Project/9x9Moves.txt";
	Random random = new Random();
	String whoWon = "no one"; //variable for if 9x9 game is done
	int learning9wins = 0, oppAIwins = 0, draws = 0;
	List<Move> moves = new ArrayList<Move>();
	List<Integer> allyMoves = new ArrayList<Integer>();
	List<Integer> opponentMoves = new ArrayList<Integer>();
	String oldBoard = server.setUpBoards(9);
	int square1 = -1;//pointer for minisquare that is being played in next

	//-----------------------------------------
	void setUpLearning9(){//Gets knowledge from text file and resets variables
		learning9wins = 0; oppAIwins= 0; draws = 0;
		moves.clear();
		String[] temp;
		try{
			String[] outputs = server.readFileNotResource(URI).split(System.lineSeparator());
			//An array of the lines within the file
			for (int i = 0; i < outputs.length; i ++){
				if (!outputs[i].isEmpty()) {
					temp = outputs[i].split(" ");//an array of parts of one line (splitting up the board, next move and value)
					moves.add(ai.new Move());
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
	//----------------------------------------------------------
	void addMoves(int newPosition){//Adds all the valid moves available in oldBoard to the moves list 
		List<Integer> validMoves = new ArrayList<Integer>();
		List<List<Integer>> validMovesList = new ArrayList<List<Integer>>();
		for (int i = 0; i < 9; i ++){
			validMovesList.add(ai.findValidMoves(oldBoard.toCharArray(), i));
		}
		for (int i = 0; i < 9; i ++){
			for (int j = 0; j < validMovesList.get(i).size(); j ++){
				validMoves.add(validMovesList.get(i).get(j));
			}
		}
		for (int i = 0; i < validMoves.size(); i ++){
			moves.add(ai.new Move());
			moves.get(newPosition + i).currBoard = oldBoard;
			moves.get(newPosition + i).nextMove = validMoves.get(i);
			moves.get(newPosition + i).value = 0;
		}
	}
	//--------------------------------------------------------
	String learning9(String board, int square, char symbol){//main part of the machine learning AI
		checkIfMoveExists(board, false);//Records opponents moves
		checkIfMoveExists(board, false);//Adding moves that appear after opponents move 
		//(ones that can potentially be chosen by learning AI)
		List<Integer> availableMoves = new ArrayList<Integer>();
		for (int i = 0; i < moves.size(); i ++){
			if (square != -1){//If the AI is restricted to one minisquare (due to rules from ultimate tic tac toe)
				if (moves.get(i).currBoard.equals(board) && Math.floor(moves.get(i).nextMove / 9) == square){
					availableMoves.add(availableMoves.size(), i);//Adds all the relevant moves from the moves list
					//the boards are the same and the next move is one within the minisquare
				}
			}
			else{//If the AI can go anywhere
				if (moves.get(i).currBoard.equals(board)){
					availableMoves.add(availableMoves.size(), i);//Adds all the moves that have the same board from
					//moves list
				}
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
		tempBoard[moves.get(availableMoves.get(position)).nextMove] = symbol;//changes the board
		board = new String(tempBoard);
		checkIfMoveExists(board, true);//Records ally move
		return board;
	}
	//------------------------------------------------
	void checkIfMoveExists(String board, boolean ally){
		int move = ai.getDiff(board, oldBoard);
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
			oldBoard = board;
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
	//--------------------------------------------
	String startRandom(String board, int square, char ally){//Chooses a random move out of all available moves
		List<Integer> validMoves = new ArrayList<Integer>();
		if (square == -1 || ai.findValidMoves(board.toCharArray(), square).size() == 0){//AI can go anywhere on board
			List<ArrayList<Integer>> validMovesLists = new ArrayList<ArrayList<Integer>>();
			for (int i = 0; i < 9; i ++) {
				validMovesLists.add((ArrayList<Integer>) ai.findValidMoves(board.toCharArray(), i));
				//Checks each minisquare and makes a list out of the available spaces for each of them
				//Then adds all of those lists into one bigger list
			}
			for (int i = 0; i < validMovesLists.size(); i ++) {
				for (int j = 0; j < validMovesLists.get(i).size(); j ++) {
					validMoves.add(validMovesLists.get(i).get(j));//Parses the list of lists into one big list that has
					//all the available moves in it
				}
			}
		}
		else {//AI can only go in one minisquare
			validMoves = ai.findValidMoves(board.toCharArray(), square);
		}
		int	move = 0;
		try{
			move = validMoves.get(random.nextInt(validMoves.size()));
		}
		catch(Exception e){
			System.out.println(board);
		}
		return changeBoard(move, board, ally);
		//randomly chooses a space and changes the board
	}
	//----------------------------------
	String startGreedyAI(String board, int square, char ally){//implements the 3x3 rule based AI
		if (square == -1){
			square = checkBestSquare(board, ally);//Needs to limit where it can play to a 3x3 board,
			//so that it can implement rule based AI, so tries to find the best minisquare to play in
		}
		String message = board;
		String miniBoard1 = board.substring(square * 9, square * 9 + 9);
		String miniBoard2 = ai.rule3(ally, miniBoard1);//implements rule based AI
		int diff = ai.getDiff(miniBoard1, miniBoard2);
		if (diff == -1){
			displayBoard(board);
		}
		message = changeBoard(square * 9 + diff, message, miniBoard2.charAt(diff));//changes board with new move
		isGameOver(message, ally);//if game is over, changes the whoWon string variable to show that game is over
		return message;
	}
	//----------------------------------------------------------------
	void changeSquare(String oldBoard, String newBoard){//After AI makes a move and changes the board
		//this updates the square variable to send back with the board to let the other side where they are able to play
		int diff = ai.getDiff(oldBoard, newBoard);
		int square = diff % 9;
		if (ai.findValidMoves(newBoard.toCharArray(), square).size() == 0){
			square1 = -1;
		}
		else{
			square1 = square;
		}
	}
	//--------------------------------------------------------------
	String testLearning9(int loop){//trains the machine learning AI
		Date date1 = new Date();
		long time = date1.getTime();
		setUpLearning9();//Imports the knowledge it gained from the text file
		Date date3 = new Date();
		System.out.println("set up learning9 time: " + (date3.getTime() - time));
		char first;
		int learning = 0, opp = 0;
		String board, board2;
		for (int i = 0; i < loop; i ++){//repeats for each game
			Date date4 = new Date();
			board = server.setUpBoards(9);//resets variables for new game
			board2 = board;
			oldBoard = board;
			if (random.nextInt(2) == 0){//randomly chooses which AI goes first
				first = 'l';
				learning ++;
			}
			else{
				first = 'o';
				opp++;
			}
			square1 = -1;
			allyMoves.clear();
			opponentMoves.clear();
			while (true){//starts playing games
				Date date10 = new Date();
				if (first == 'l'){
					board2 = learning9(board, square1, 'X');
					board2 = changeIfCaptured(ai.getDiff(board, board2), board2, 'X');//replaces some minisquare symbols
					//with asterisks if captured
					changeSquare(board, board2);//changes the square pointer, so next player knows where they can play
					if (checkIfGameOver(board2, 'X')){//checks if game over, if it is, then changes values of moves
						//so that the machine learning AI 'learns'
						break;
					}
					board = board2;//one is the board before the move and one is after, so that changeSquare can be done
					//resets variable for the next move
					board2 = startRandom(board, square1, 'O');//random AI
					board2 = changeIfCaptured(ai.getDiff(board, board2), board2, 'O');
					changeSquare(board, board2);
					if (checkIfGameOver(board2, 'X')){
						break;
					}
					board = board2;
				}
				else{
					board2 = startRandom(board, square1, 'X');
					board2 = changeIfCaptured(ai.getDiff(board, board2), board2, 'X');
					changeSquare(board, board2);
					if (checkIfGameOver(board2, 'O')){
						break;
					}
					board = board2;
					board2 = learning9(board, square1, 'O');
					board2 = changeIfCaptured(ai.getDiff(board, board2), board2, 'O');
					changeSquare(board, board2);
					if (checkIfGameOver(board2, 'O')){
						break;
					}
					board = board2;
				}
				Date date11 = new Date();
				System.out.println("2 moves time: " + (date11.getTime() - date10.getTime()));
			}
			Date date5 = new Date();
			System.out.println("Game time: " + (date5.getTime() - date4.getTime()));
		}
		Date date6 = new Date();
		writeFile(URI);//writes knowledge into text file
		Date date2 = new Date();
		System.out.println("Write to file time: " + (date2.getTime() - date6.getTime()));
		System.out.println("Whole function time: " + (date2.getTime() - time) + " milliseconds");
		return "Times that learning AI won: " + learning9wins + "<br>Times that random AI won: " + oppAIwins + "<br>"
		+ "Draws: " + draws + "<br>Total games: " + (learning9wins + oppAIwins + draws)
		+ "<br>Times learning AI went first: " + learning + "<br>Times random AI went first: " + opp;
		//returns statistic to webpage
	}
	//------------------------------------------------------------------
	void writeFile(String URI){//writes entire moves list into one file
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
	//-----------------------------------------------------------
	boolean checkIfGameOver(String board, char learning){
		List<Integer> winner, loser;
		if (ai.isCaptured(new String(simplifyBoard(board.toCharArray())), 'X')){
			//checks if the big 3x3 board has been captured by X's
			if (learning == 'O'){//finds out who wins and loses
				loser = allyMoves;
				winner = opponentMoves;
				oppAIwins ++;
			}
			else{
				loser = opponentMoves;
				winner = allyMoves;
				learning9wins ++;
			}
			for (int i = 0; i < winner.size(); i ++){
				moves.get(winner.get(i)).value += 3;//changes values here ('learns')
			}
			for (int i = 0; i < loser.size(); i ++){
				moves.get(loser.get(i)).value -= 3;
			}
			return true;//X's won
		}
		else if (ai.isCaptured(new String(simplifyBoard(board.toCharArray())), 'O')){//checks if O's won
			if (learning == 'O'){
				loser = opponentMoves;
				winner = allyMoves;
				learning9wins ++;
			}
			else{
				loser = allyMoves;
				winner = opponentMoves;
				oppAIwins ++;
			}
			for (int i = 0; i < winner.size(); i ++){
				moves.get(winner.get(i)).value += 3;//learns
			}
			for (int i = 0; i < loser.size(); i ++){
				moves.get(loser.get(i)).value -= 3;
			}
			return true;//O's won
		}
		for (int i = 0; i < board.length(); i ++){
			if (board.charAt(i) == '_'){
				return false;//can still play
			}
		}
		draws ++;
		for (int i = 0; i < opponentMoves.size(); i ++){
			moves.get(opponentMoves.get(i)).value ++;
			//can encourage AI to draw by increasing value or discourage by decreasing value here
		}
		for (int i = 0; i < allyMoves.size(); i ++){
			moves.get(allyMoves.get(i)).value ++;
		}
		return true;//draw
	}
	//------------------------------------------------------------
	String test(int loop){//test to see how well the minimax AI did against the greedy AI (one that incorporated 3x3 rule AI)
		String board;
		String tempBoard;
		int wins = 0, losses = 0, draws = 0, smartFirst = 0;
		String first;
		for (int i = 0; i < loop; i ++) {//repeats for as many games specified
			whoWon = "no one";
			square1 = -1;
			board = server.setUpBoards(9);//resets variables every game
			if (random.nextInt(2) == 0){//randomly chooses who goes first
				first = "smart";
				smartFirst ++;
			}
			else{
				first = "random";
			}
			while (whoWon == "no one"){
				if (first.equals("random")){
					tempBoard = startRandom(board, square1, 'X');
					changeSquare(board, tempBoard);
					board = tempBoard;
					isGameOver(board, 'X');
					if (whoWon == "no one"){
						tempBoard = smartAI(board, square1, 'O');
						changeSquare(board, tempBoard);
						board = tempBoard;
						isGameOver(board, 'O');
					}
				}
				else{
					tempBoard = smartAI(board, square1, 'X');
					changeSquare(board, tempBoard);
					board = tempBoard;
					isGameOver(board, 'X');
					if (whoWon == "no one"){
						tempBoard = startRandom(board, square1, 'O');
						changeSquare(board, tempBoard);
						board = tempBoard;
						isGameOver(board, 'O');
					}
				}
			}
			if ((whoWon.equals("X") && first.equals("smart")) || (whoWon.equals("O") && first.equals("random"))){
				wins ++;//minimax AI wins
			}
			else if ((whoWon.equals("X") && first.equals("random")) || whoWon.equals("O") && first.equals("smart")){
				losses ++;//minimax AI loses
			}
			else if (whoWon.equals("drawn")){
				draws ++;
			}
			else{
				System.out.println("GAME FINISHED WITHOUT CONCLUSION");
				//if it wasnt a draw, win or loss (for debugging purposes)
			}
		}
		return "Total Games: " + (losses + wins + draws) + "<br>wins: " + wins + "<br>Times that smartAI went first: " 
		+ smartFirst + "<br>losses: " + losses + "<br>draws: " + draws;//returns stats to webpage
	}
	//-------------------------------------------------------------
	void displayBoard(String board){//displays the 9x9 board clearly to the console
		for (int m = 0; m < 3; m ++){
			for (int k = 0; k < 3; k ++){
				for (int i = 0; i < 3; i ++){
					for (int j = 0; j < 3; j ++){
						System.out.print(board.charAt(i * 9 + j + k * 3 + m * 27));
					}
					System.out.print("|");
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println("------------------");
	}
	//------------------------------------------------------------
	char isGameDone(String board, char ally){
		char[] simpleBoard = simplifyBoard(board.toCharArray());
		if (ai.isCaptured(new String(simpleBoard), ally)){
			return ally;//if the symbol input has won, then returns that same symbol
		}
		else if (isDrawn(new String(simpleBoard))){
			return '_';//if the game was a draw
		}
		else{
			return ' ';//if the game is unfinished or if the game was won by the other symbol
		}
	}
	//-----------------------------------------------
	boolean isDrawn(String board){//Checks if the board is full, if it is, then it is a draw
		for (int i = 0; i < board.length(); i ++){
			if (board.charAt(i) == '_'){
				return false;
			}
		}
		return true;
	}
	//-----------------------------------
	String smartAI(String board, int square, char ally){//minimax AI
		if (square == -1){
			square = checkBestSquare(board, ally); //Simplifies problem to only worry about a 3x3 board of where to move
		}
		String message = makeBestMove(board, ai.findValidMoves(board.toCharArray(), square), -1000, square, 0, 0, ally);
		//Then uses this recursive algorithm to find a move
		return message;
	}
	//---------------------------------------------------------------
	String makeBestMove(String board, List<Integer> validMoves, int best, int square, int position, int counter, char ally){
		char opponent;
		if (counter == validMoves.size()){//Iterates through all the valid moves and if it reaches the end, it stops
			server.setSquare(position % 9);//sets the next minisquare pointer
			return changeBoard(position, board, ally);//changes the board
		}
		if (ally == 'O'){
			opponent = 'X';
		}
		else{
			opponent = 'O';
		}
		String Board = changeBoard(validMoves.get(counter), board, ally);//changes the board with the first valid move
		List<Integer> opponentMoves = ai.findValidMoves(Board.toCharArray(), getSquare(Board, validMoves.get(counter), ally));
		//gets a list of the possible moves for the opponent after this move has been played
		//getSquare function gets the minisquare that can be played in, if it can play anywhere, then it uses checkBestSquare
		String Board2 = findBestMove(Board, opponentMoves, 0, opponent, -1000, 0);
		//simply chooses the move that gives the most value to the board (for the opponent), a greedy algorithm
		if (evaluateBoard(Board2.toCharArray(), ally) > best){//so at this point, AI has chosen the first valid move,
			//then played a response to it from the opponent, getting the most valued move at that point (greedy)
			//this then eventually chooses the valid move that leads to the best value after the opponents move
			best = evaluateBoard(Board2.toCharArray(), ally);
			position = validMoves.get(counter);
		}
		return makeBestMove(board, validMoves, best, square, position, counter + 1, ally);
		//calls itself, adding 1 to the counter
	}
	//--------------------------------------------------------------
	int getSquare(String board, int move, char ally){//returns the minisquare pointer
		//uses checkBestSquare algorithm if the AI can play anywhere and then returns resulting minisquare pointer
		int square = 0;
		if (move == -1){
			return -1;
		}
		else{
			square = move % 9;
			if (ai.findValidMoves(board.toCharArray(), square).isEmpty()){
				//the minisquare that is being pointed to is full, so must find another minisquare
				square = checkBestSquare(board, ally);
			}
			else{
				square = move % 9;
			}
			return square;
		}
	}
	//--------------------------------------------------------------
	String findBestMove(String board, List<Integer> validMoves, int counter, char ally, int best, int pos){
		//a greedy way of finding the best move, a recursive algorithm
		if (counter == validMoves.size()){//ends the algorithm here
			return changeBoard(pos, board, ally);
		}
		String Board = changeBoard(validMoves.get(counter), board, ally);//changes the board using one of the valid moves
		int value = evaluateBoard(Board.toCharArray(), ally);//finds the value of the board after this move
		if (value > best){
			best = value;
			pos = validMoves.get(counter);//eventually keeps store of the best valued move
		}
		return findBestMove(board, validMoves, counter + 1, ally, best, pos);//calls itself, adding 1 to counter
	}
	//--------------------------------------------------------------
	String changeBoard(int position, String board, char ally){//edits the board with a given position to edit
		char[] Board = board.toCharArray();
		Board[position] = ally;
		return changeIfCaptured(position, new String(Board), ally);//Adds in the asterisks if the minisquare has been captured
	}
	//-------------------------------------------------------------
	String changeIfCaptured(int position, String board, char ally){
		//checks if the minisquare was captured
		//if it is captured, then changes the board (changes every space to an asterisk that is blank)
		char opp;
		if (ally == 'O')
			opp = 'X';
		else
			opp = 'O';
		int start = position - (position % 9);
		String miniBoard = board.substring(start, start + 9);
		char[] Board = board.toCharArray();
		if (ai.isCaptured(miniBoard, ally) == true){
			for (int i = 0; i < 9; i ++){
				if (Board[start + i] == '_' || Board[start + i]  == opp){
					Board[start + i] = '*';
				}
			}
		}
		return new String(Board);
	}
	//-------------------------------------------------------------
	void isGameOver(String board, char ally){//Changes variable whoWon if game is over due to a draw or the input symbol winning
		if (isGameDone(board, ally) == ally){
			whoWon = ally + "";
		}
		else if (isGameDone(board, ally) == '_'){
			whoWon = "drawn";
		}
	}

	//--------------------------------------------------------------
	int evaluateBoard(char[] board, char ally){//calculates a value from the given board and symbol
		int value = 0;
		char[] simpleBoard = simplifyBoard(board);//Turns the board into a 3x3 board using the minisquares
		//A minisquare is considered blank unless it has been captured
		for (int i = 0; i < 9; i ++){
			if (simpleBoard[i] == '_'){//If current minisquare isn't captured yet
				String miniBoard = "";
				for (int j = 0; j < 9; j ++){
					miniBoard = miniBoard + board[(i * 9) + j];
				}
				int result = evaluateMiniSquare(miniBoard, ally, false);//evaluates the current minisquare
				char symbol;
				if (result < 0){
					symbol = 'X';
				}
				else{
					symbol = 'O';
				}
				value = value + result * ai.findWinningPossibilities(i, symbol, simpleBoard, false);
				//multiplies the result of the evaluateMiniSquare function with how many winning lines it is apart of 
				//in the big 3x3 board of minisquares
			}
		}
		value += evaluateMiniSquare(new String(simpleBoard), ally, true) * 100;
		//Evaluates the 3x3 board of minisquares and multiplies it by 100, giving it more weight
		//This is the most important thing, the big 3x3 board of minisquares; whoever wins this, wins the game
		return value;
	}
	//-------------------------------------------------------------------------
	int checkBestSquare(String board, char ally){//Finding the best square to play in
		int result;
		result = findIfSquareCanBeWon(board, ally);//Finds if a minisquare can be won. If so, returns that minisquare
		//if multiple, returns most valuable square
		if (result != -1){
			return result;
		}
		result = findKeyMinisquare(board);//Finds if there is a 2 in a row for either player
		//and finds the square that completes the row (there may be multiple 2 in a rows, so multiple squares will appear here)
		//then the algorithm returns the most valuable square regardless of if it is for opponent or ally
		//most valuable square is calculated with findMostValuableSquare algorithm
		if (result != -1){
			return result;
		}
		char opponent;
		if (ally == 'X'){
			opponent = 'O';
		}
		else{
			opponent = 'X';
		}
		result = findIfSquareCanBeWon(board, opponent);//Checks if there is a minisquare that the opponent can win
		//if there is, returns that square
		//if multiple, returns most valuable square
		if (result != -1){
			return result;
		}
		result = getMostValuableAvailableSquare(board);//last resort, looks at all available squares
		//if only 1, returns that one
		//if multiple, returns most valuable square
		if (result != -1){
			return result;
		}
		else{
			whoWon = "drawn";//draw because all the minisquares are filled up
			return -1;
		}
	}
	//------------------------------------------------------------------
	int getMostValuableAvailableSquare(String board){//looks at all available squares and returns most valuable square
		char[] simple = simplifyBoard(board.toCharArray());//gets 3x3 board of minisquare
		List<Integer> pos = new ArrayList<Integer>();
		for (int i = 0; i < 9; i ++){
			if (simple[i] == '_'){
				pos.add(i);//gets list of available minisquares
			}
		}
		return findMostValuableSquare(pos);
	}
	//--------------------------------------------------------------------
	int findKeyMinisquare(String board){//finds the last minisquares in the 2 in a row and returns most valuable square
		char[] simple = simplifyBoard(board.toCharArray());
		List<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < 9; i ++){
			if (simple[i] == 'X'){
				positions.add(i);//gets all the minisquares captured by X
			}
		}
		List<Integer> x = ai.find2InRow(positions, simple);//finds any 2 in a row for X and gets all the final squares in those rows
		positions.clear();
		for (int i = 0; i < 9; i ++){
			if (simple[i] == 'O'){
				positions.add(i);//gets all the minisquares captured by O
			}
		}
		List<Integer> o = ai.find2InRow(positions, simple);//repeats for O
		for (int i = 0; i < o.size(); i ++){
			x.add(o.get(i));//congregates all the final squares
		}
		if (x.size() == 0){
			return -1;//if there are no final squares, returns -1, signalling that there is nothing
		}
		return findMostValuableSquare(x);//returns most valuable square out of list
	}
	//---------------------------------------------------------------------
	int findIfSquareCanBeWon(String board, char symbol){//Finds if a minisquare can be won
		String miniBoard;
		List<Integer> squares = new ArrayList<Integer>();
		List<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < 9; i ++){
			miniBoard = board.substring(i * 9, i * 9 + 9);//cycles through each minisquare
			for (int j = 0; j < 9; j ++){
				if (miniBoard.charAt(j) == symbol){
					positions.add(j);
				}
			}
			if (ai.find2InRow(positions, miniBoard.toCharArray()).size() != 0){
				squares.add(i);//gets all the minisquares that are able to be captured in one turn
			}
		}
		if (squares.size() == 0){
			return -1;
		}

		return findMostValuableSquare(squares);
	}
	//------------------------------------------------------------------
	int findMostValuableSquare(List<Integer> squares){//from a list of minisquares, chooses best one
		int best = 0;
		int pos = -1, result;
		for (int i = 0; i < squares.size(); i ++){
			result = interpretPosition(squares.get(i));//gets a result based on:
			//if corner minisquare, value = 3
			//if middle minisquare, value = 4,
			//if edge minisquare, value = 2 (the value is how many lines the square is apart of)
			if (result > best){
				best = result;
				pos = squares.get(i);//eventually gets most valuable square (after cycling through all of them)
			}
			else if (result == best){
				if (random.nextInt(2) == 0){//randomly replaces the best square, if the value of another is the same
					//adds in variety and randomness, so less predictable
					pos = squares.get(i);
				}
			}
		}
		return pos;
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
	//---------------------------------------------------------------------	
	int evaluateMiniSquare(String board, char ally, boolean big){//making values for each mini square
		//the big variable signals whether if this algorithm is for a specific minisquare(false) 
		//or the 3x3 board of minisquares(true)
		char opponent;
		List<Integer> enemyPositions = new ArrayList<Integer>(), allyPositions = new ArrayList<Integer>();
		int value = 0;
		if (ally == 'X'){
			opponent = 'O';
		}
		else{
			opponent = 'X';
		}
		if (ai.isCaptured(board, ally)){
			//if 3x3 is captured by ally, 100 is returned straight away
			return 100;
		}
		else if (ai.isCaptured(board, opponent)){
			//if 3x3 is captured by opponent, -100 is returned straight away
			return -100;
		}
		for (int i = 0; i < 9; i ++){
			int result;
			if (board.charAt(i) == opponent){
				result = ai.findWinningPossibilities(i, opponent, board.toCharArray(), false);
				if (big == true && result == 0){//discourages the AI to allow the opponent to capture a minisquare
					//even if it isn't apart of any available lines
					value -= 0.1;
				}
				else{
					value -= result;//subtracts how many lines the opponent can win from to the value
				}
				enemyPositions.add(i);//adds the position to a list of enemy positions
			}
			else if (board.charAt(i) == ally){//repeats same procedure but for ally positions
				//adds to value rather that subtracting
				result = ai.findWinningPossibilities(i, ally, board.toCharArray(), false);
				if (big == true && result == 0){
					value += 0.1;
				}
				else{
					value += result;
				}
				allyPositions.add(i);
			}
		}
		for (int i = 0; i < ai.find2InRow(allyPositions, board.toCharArray()).size(); i ++){
			//for every 2 in a row that is found, 10 is added to the value (having 2 in a row is very valuable)
			value += 10;
		}
		for (int i = 0; i < ai.find2InRow(enemyPositions, board.toCharArray()).size(); i ++){
			//repeats for opponent, but subtracts 10
			value -= 10;
		}
		return value;
	}
	//-----------------------------------------------------------
	char[] simplifyBoard(char[] board){//takes the 9x9 board and returns a simpler 3x3 board,
		//where each square represents a minisquare in the 9x9 board
		char[] simpleBoard = new char[9];
		for (int i = 0; i < 9; i ++){
			int o = 0;
			int x = 0;
			int ast = 0;
			for (int j = 0; j < 9; j ++){
				if (board[i * 9 + j] == 'O'){
					o++;//accounts for O, X and *
				}
				else if (board[i * 9 + j] == 'X'){
					x++;
				}
				else if (board[i * 9 + j] == '*'){
					ast++;
				}
				if (x + ast == 9){
					simpleBoard[i] = 'X';//minisquare is captured by X
				}
				else if (o + ast == 9){
					simpleBoard[i] = 'O';//minisquare is captured by O
				}
				else if (x + o == 9){
					simpleBoard[i] = 'd';//signalling a draw
				}
			}
			if (simpleBoard[i] != 'X' && simpleBoard[i] != 'O' && simpleBoard[i] != 'd'){
				simpleBoard[i] = '_';//only other option is that there are still spaces available and so it's empty
			}
		}
		return simpleBoard;
	}
}
