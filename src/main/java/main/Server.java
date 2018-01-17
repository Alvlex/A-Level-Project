package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class Server{

	String nineBoard = setUpBoards(9);//starts out as an 81 character string of just _
	String threeBoard = setUpBoards(3);//9 char string of _
	int square = -1;
	//these variables are used to interface with the client. As the board gets received, these variables get updated
	//and used in the website class
	char[] cypherChars = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s',
			't','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q',
			'R','S','T','U','V','W','X','Y','Z','*','_'};
	//array of characters used in the caesar cypher used on both server and client side

	String setUpBoards(int size){//creates a size * size string of just blanks (_)
		String board = "";
		for (int i = 0; i < size; i ++){
			for (int j = 0; j < size; j ++){
				board += "_";
			}
		}
		return board;
	}
	//functions below are used to set and get the variables used in the website class
	void setNineBoard(String board){
		nineBoard = board;
	}
	String getNineBoard(){
		return nineBoard;
	}
	void setThreeBoard(String board){
		this.threeBoard = board;
	}
	String getThreeBoard(){
		return threeBoard;
	}
	void setSquare(int square){
		this.square = square;
	}
	int getSquare(){
		return square;
	}
	//-------------------------------------------------------------------
	String decode(String cypher, int offset){//caesar cypher decoding algorithm
		char[] cypherArray = cypher.toCharArray();
		for (int i = 0; i < cypher.length(); i ++){//for each character in the cypher received
			int newPositionInArray = findCharInArray(cypherArray[i]) - (offset * i);//calculates the new position of each char
			//does this by getting initial position and subtracting it by the offset (parameter) multiplied by
			//the position in the cypher
			while (newPositionInArray < 0){//if this new position is below 0, then wrap it around the cypher chars
				//(like going from A back round to Z), so add on the length of cypherchars until the number is positive
				//so it will be a position within the cypherchars array
				newPositionInArray += cypherChars.length;
			}
			cypherArray[i] = cypherChars[newPositionInArray];//replaces the letter with the new letter
		}
		return new String(cypherArray);
	}
	//-----------------------------------------------------------------
	String encode(String text, int offset){//encoding algorithm
		char[] textArray = text.toCharArray();
		for (int i = 0; i < text.length(); i ++){
			int newPositionInArray = findCharInArray(textArray[i]) + (offset * i);
			//like decoding, except is adds the offset multiplied by the position in the text (rather than subtracting)
			while(newPositionInArray >= cypherChars.length){//while the new position is above or equal to the length
				newPositionInArray -= cypherChars.length;//makes the new position an actual position within cypherchars
			}
			textArray[i] = cypherChars[newPositionInArray];//replaces letter
		}
		return new String(textArray);
	}
	//-------------------------------------------------------------------
	int findCharInArray(char letter){//returns the position of the character when found within the cypherchars array
		for (int i = 0; i < cypherChars.length; i ++){
			if (letter == cypherChars[i]){
				return i;//returns position of matching letter
			}
		}
		return -1;
	}
	//-----------------------------------------------------------------
	String readFile(String file){//reads file in the resource folder (relative path, not hard to find, but cant write to the file)
		String everything = "";
		InputStream in = getClass().getResourceAsStream(file);//sets up inputstream with the file specified
		BufferedReader br = new BufferedReader(new InputStreamReader(in));//gets a reader set up
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();//reads first line

			while (line != null) {//if line isn't nothing
				sb.append(line);//adds the line to the string builder
				sb.append(System.lineSeparator());//adds a line separator
				line = br.readLine();//reads the next line
			}
			everything = sb.toString();//parses the string builder into a string
			br.close();//closes reader
		} catch (IOException e) {
			e.printStackTrace();
		}
		return everything;//returns the string
	}
	//------------------------------------------------------------------
	String readFileNotResource(String URI){//reads a file not in the resource folder
		//can also write to these files since they're not in resource folder
		//basically like before but setup is different
		Date date1 = new Date();
		try{
			String everything = "";
			URI uri = new URI(URI);//parses the input URI
			File file = new File(uri);//finds the file
			InputStream in = new FileInputStream(file);//sets up the inputstream
			BufferedReader br = new BufferedReader(new InputStreamReader(in));//sets up reader
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null && line != "") {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
			br.close();
			Date date2 = new Date();
			System.out.println("Read file time: " + (date2.getTime() - date1.getTime()));
			return everything;
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
	}
}
