package threeChess.agents;

import java.util.*;
import threeChess.*;

public class LegalAgent extends Agent {

    private String name = "Legal";
    private HashMap<Position,Piece> ourboard = new HashMap<Position,Piece>();

    /**
     * A no argument constructor, required for tournament management.
     **/
    public LegalAgent() {
    }

    /**
     * Play a move in the game. The agent is given a Board Object representing the
     * position of all pieces, the history of the game and whose turn it is. They
     * respond with a move represented by a pair (two element array) of positions:
     * the start and the end position of the move.
     * 
     * @param board The representation of the game state.
     * @return a two element array of Position objects, where the first element is
     *         the current position of the piece to be moved, and the second element
     *         is the position to move that piece to.
     **/
    public Position[] playMove(Board board) {
        Colour turn = board.getTurn();
        updatePositions(board, turn, ourboard);
        Set<Position[]> moves = makeLegalMoves(board);
        Position[] r_move = moves.stream().skip(new Random().nextInt(moves.size())).findFirst().orElse(null);
        ourboard.remove(r_move[0]);
        ourboard.put(r_move[1], board.getPiece(r_move[0]));
        return r_move;
    }

    /**
     * Generates a set of Position arrays which are all legal moves in the current
     * state of the game board.
     * 
     * @param board The representation of the game state.
     * @return a set of Position arrays {start, end} which are all legal moves in
     *         the current game state.
     */
    public Set<Position[]> makeLegalMoves(Board board) {
        Set<Position[]> legalMoves = new HashSet<>();
        Set<Position> pieces = ourboard.keySet();
        Position end = pieces.iterator().next();
        for (Position square : pieces) {
            Piece mover = board.getPiece(square);
            Direction[][] steps = mover.getType().getSteps();
            int reps = mover.getType().getStepReps();
            end = square;
            for (Direction[] step : steps) {
                end = square;
                for (int i = 0; i < reps; i++) {
                    try {
                        if (board.isLegalMove(square, board.step(mover, step, end))) {
                            end = board.step(mover, step, end);
                            legalMoves.add( new Position[] { square, end });
                        }
                    } catch (ImpossiblePositionException e) {
                    }
                }
            }
        }
        return legalMoves;
    }

    public void updatePositions(Board board, Colour c, HashMap<Position,Piece> thePos){
        if(board.getMoveCount() < 3){
            try{
                thePos.put(Position.get(c,0,0),new Piece(PieceType.ROOK,c)); thePos.put(Position.get(c,0,7), new Piece(PieceType.ROOK,c));
                thePos.put(Position.get(c,0,1),new Piece(PieceType.KNIGHT,c)); thePos.put(Position.get(c,0,6), new Piece(PieceType.KNIGHT,c));
                thePos.put(Position.get(c,0,2),new Piece(PieceType.BISHOP,c)); thePos.put(Position.get(c,0,5), new Piece(PieceType.BISHOP,c));
                thePos.put(Position.get(c,0,3),new Piece(PieceType.QUEEN,c)); thePos.put(Position.get(c,0,4), new Piece(PieceType.KING,c));
                for(int i = 0; i<8; i++){
                    thePos.put(Position.get(c,1,i), new Piece(PieceType.PAWN,c));
                }
            } catch(ImpossiblePositionException e){}
            } else {
                int numMoves = board.getMoveCount();
                Position[] prev = board.getMove(numMoves-1);
                Position[] prev2 = board.getMove(numMoves-2);
                thePos.remove(prev2[1]);
                thePos.remove(prev[1]);
            }
        
    }

     /**
    * Returns a deep clone of the board state, 
    * such that no operations will affect the original board instance.
    * @return a deep clone of the board state casted to a board Object type.
    * **/ 
    public Board getGame(Board board) throws CloneNotSupportedException{
        return (Board)board.clone();
    }
  
    /**
     * @return the Agent's name, for annotating game description.
     * **/ 
    public String toString(){
        return name;
    }
  
    /**
     * Displays the final board position to the agent, 
     * if required for learning purposes. 
     * Other a default implementation may be given.
     * @param finalBoard the end position of the board
     * **/
    public void finalBoard(Board finalBoard){
    }
}
