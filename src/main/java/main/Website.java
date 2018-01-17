package main;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Website 
{
	@Autowired
	Server server = new Server();

	@Autowired
	ClientSide clientSide = new ClientSide();

	@Autowired
	AI ai = new AI();
	
	@Autowired
	AI9 ai9 = new AI9();
	
	String endMessage = "<div class=\"popup\" id=\"popup1\">"//popup for after a game is over
			//gives the option of returning to the menu or starting a new game
			+ "<div style=\"height:60px; width=100%;\"><p style=\"vertical-align:center; vertical-align:middle;"
			+ " line-height:60px\" id=\"output\"></p></div>"//output is here so that i can dynamically change the message
			//using javascript, so i can output the right winner every time
			+ "<div style=\"height:60px; width:100%;\">"
			+ "<button style=\"text-align:center; vertical-align:middle; line-height:60px; height:60px; "
			+ "width:100%; cursor:pointer;\" onclick=\"resetIt(true)\" >Main Menu</button>"//main menu button (also resets game)
			+ "</div><div style=\"height:60px; width:100%;\">"
			+ "<button style=\"cursor:pointer; height:100%; width:100%;"
			+ "\" type=\"button\" onclick=\"resetIt(false)\">Reset Game"//reset game button
			+ "</button></div></div>"
			+ "<script>"
			+ System.lineSeparator()//more readability in the file
			+ "var cypherChars = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s',"
			+ "'t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q',"
			+ "'R','S','T','U','V','W','X','Y','Z','*','_'];"//array for the caesar cypher functions
			+ System.lineSeparator()
			
			+ "function encode(text, offset){"//FUNCTION encoding the text into the caesar cypher
			+ "var textArray = text.split('');"
			+ "for (i = 0; i < text.length; i ++){"
			+ "var newPositionInArray = cypherChars.indexOf(textArray[i]) + (offset * i);"
			+ "while(newPositionInArray >= cypherChars.length){"
			+ "newPositionInArray -= cypherChars.length;"
			+ "}"
			+ "textArray[i] = cypherChars[newPositionInArray];"
			+ "}"
			+ "return textArray.join('');"
			+ "}"
			
			+ "function decode(cypher, offset){"//FUNCTION decoding the caesar cypher
			+ "var cypherArray = cypher.split('');"
			+ "for (i = 0; i < cypher.length; i ++){"
			+ "var newPositionInArray = cypherChars.indexOf(cypherArray[i]) - (offset * i);"
			+ "while(newPositionInArray < 0){"
			+ "newPositionInArray += cypherChars.length;"
			+ "}"
			+ "cypherArray[i] = cypherChars[newPositionInArray];"
			+ "}"
			+ "return cypherArray.join('');"
			+ "}"
			+ "</script></body></html>";//message for after every client side game webpage

	@RequestMapping("/")//main menu (can access it through root)
	String main(){//html, css and JS are in the file 'mainPage.txt'
		return server.readFile("/mainPage.txt");
	}
	@RequestMapping("/game/3/pvp/player1")//3x3 game, 1st player web page for pvp (can access all these through link on main menu)
	String three1(){//css and start of js is in file, html table is made from clientSide, endMessage is more js.
		return server.readFile("/3x3.txt") + clientSide.create3Board(server.getThreeBoard().toCharArray(), 0) + endMessage;
	}
	@RequestMapping("/game/9/pvp/player1")//9x9 game, 1st player web page for pvp
	String nine1(){//same components as the 3x3, 1st player, pvp webpage
		return server.readFile("/9x9.txt") + clientSide.create9Board(server.getNineBoard().toCharArray()) + endMessage;
	}
	@RequestMapping("/game/3/pvp/player2")//3x3 game, 2nd player, pvp
	String three2(){
		return server.readFile("/3x3.txt") + clientSide.create3Board(server.getThreeBoard().toCharArray(), 0) + endMessage;
	}
	@RequestMapping("/game/9/pvp/player2")//9x9 game, 2nd player, pvp
	String nine2(){
		return server.readFile("/9x9.txt") + clientSide.create9Board(server.getNineBoard().toCharArray()) + endMessage;
	}
	@RequestMapping("/server")//3x3 board is sent to here by the client
	void hub(@RequestParam(value="board", required=true) String board) {
		server.setThreeBoard(server.decode(board, 3));//sets the board in server
	}
	@RequestMapping("/reset")//receives a html post request with the option variable
	String reset(@RequestParam String option){
		if (option.equals("3x3")){//3x3 pvp game (the files are the same for both p1 and p2)
			server.setThreeBoard(server.setUpBoards(3));//resets the board
			return server.readFile("/3x3.txt") + clientSide.create3Board(server.getThreeBoard().toCharArray(), 0) + endMessage;
			//returns a blank new version of the same file to restart the game
		}
		else if (option.equals("3AI")){//3x3 AI game
			server.setThreeBoard(server.setUpBoards(3));//resets the 3x3 board
			return server.readFile("/AI_3x3.txt") + clientSide.create3Board(server.getThreeBoard().toCharArray(), 0) + endMessage;
		}
		else if (option.equals("9x9")){//9x9 pvp game
			server.setNineBoard(server.setUpBoards(9));//resets the 9x9 board
			server.setSquare(-1);//resets the minisquare pointer
			return server.readFile("/9x9.txt") + clientSide.create9Board(server.getNineBoard().toCharArray()) + endMessage;
		}
		else if (option.equals("9AI")){//9x9 AI game
			server.setNineBoard(server.setUpBoards(9));//resets the 9x9 board
			server.setSquare(-1);//resets the minisquare pointer
			return server.readFile("/AI_9x9.txt") + clientSide.create9Board(server.getNineBoard().toCharArray()) + endMessage;
		}
		else if (option.equals("3")){//resetting the 3x3 board without getting a webpage back
			server.setThreeBoard(server.setUpBoards(3));//resets the 3x3 board
			return "";
		}
		else if(option.equals("9")){//resetting the 9x9 board without getting a webpage back 
			//(used by the reset buttons from main menu)
			server.setNineBoard(server.setUpBoards(9));//resets the 9x9 board
			server.setSquare(-1);
			return "";
		}
		else{
			return "FAILURE";
		}
	}
	@RequestMapping(value = "/server9", method = RequestMethod.POST)//where the 9x9 board is sent to
	void hub2(@RequestParam Map<String, String> requestParams){
		String board = requestParams.get("board");
		int square = Integer.parseInt(requestParams.get("square"));
		server.setNineBoard(server.decode(board, 3));//sets the 9x9 board
		server.setSquare(square);//sets the minisquare pointer
	}
	@RequestMapping("/game/3/ai/player1")//3x3 AI client side webpage interface for player 1
	String clientSide3AI1(){
		//components same as the pvp game webpages (different files though)
		return server.readFile("/AI_3x3.txt") + clientSide.create3Board(server.getThreeBoard().toCharArray(), 0) + endMessage;
	}
	@RequestMapping("/game/3/ai/player2")//for player 2
	String clientSide3AI2() {
		return server.readFile("/AI_3x3.txt") + clientSide.create3Board(server.getThreeBoard().toCharArray(), 0) + endMessage;
	}
	@RequestMapping("/game/9/ai/player1")//9x9 AI client side webpage interface
	String clientSide9AI(){
		//components same as the pvp game webpages (different files though)
		return server.readFile("/AI_9x9.txt") + clientSide.create9Board(server.getNineBoard().toCharArray()) + endMessage;
	}
	@RequestMapping("/AI/1")//If the player is O, then the client sends a get request to here, retrieving the move from the AI
	//this is after the client sends a post request to the server updating the board
	String ai1(){
		String message = ai.rule3('X', server.getThreeBoard());
		server.setThreeBoard(message);
		return server.encode(message, 3);
	}
	@RequestMapping("/AI/2")//Same but for if the player is X
	String ai2(){
		String message = ai.rule3('O', server.getThreeBoard());
		server.setThreeBoard(message);
		return server.encode(message, 3);
	}
	@RequestMapping("/test")//this trains the 3x3 machine learning AI, just have to visit this page to activate it
	//returns stats to the webpage
	String test(){
		return ai.testLearning3(500);
	}
	@RequestMapping("/test9")//this trains the 9x9 machine learning AI, just have to visit this page to activate it
	//returns stats to the webpage
	String test9(){
		return ai9.testLearning9(10);
	}
	@RequestMapping("/test2")
	String test2(){
		return ai9.test(1000);
	}
	@RequestMapping("/test3")
	String test3(){
		return ai.testRule(1000);
	}
	@RequestMapping("/AI/3")//this is the minimax AI for 9x9, the client sends a get request to this page, then this returns the new board
	String ai3() {
		String board = ai9.smartAI(server.getNineBoard(), server.getSquare(), 'O');
		server.setNineBoard(board);
		String message = server.encode(board, 3) + "," + server.getSquare();
		System.out.println(message);
		return message;
	}
	@RequestMapping("/board/9")//send a get request and this returns the current 9x9 board (encoded) and minisquare pointer
	String board9(){
		return server.encode(server.getNineBoard(), 3) + "," + server.getSquare();
	}
	@RequestMapping("/board/3")//send a get request and this returns the current 3x3 board (encoded)
	String board3(){
		return server.encode(server.getThreeBoard(), 3);
	}
	@RequestMapping("/test1")//test for time, put the function you want to test in the middle of the two date variables
	String test1(){
		Date date1 = new Date();
		ai9.setUpLearning9();
		Date date2 = new Date();
		return (date2.getTime() - date1.getTime()) + " milliseconds";
	}
	@RequestMapping("/game/info")//get an update on the current 3x3 board and 9x9 board
	String info(){
		String threeBoard = "3x3 board: " + server.getThreeBoard();
		String nineBoard = "9x9 board: " + server.getNineBoard();
		return threeBoard + "<br><br>" + nineBoard;
	}
}
