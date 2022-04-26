package threeChess.agents;

import java.util.*;
import threeChess.*;

public class MCTSAgent extends Agent {

    private String name = "mcts";
    private final Direction[] neighbours = { Direction.FORWARD, Direction.BACKWARD, Direction.LEFT, Direction.RIGHT };
    private final Colour[] colours = { Colour.BLUE, Colour.GREEN, Colour.RED };
    private static final Random random = new Random();

    /**
     * A no argument constructor, required for tournament management.
     **/
    public MCTSAgent() {
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
        try {
            Board x = getGame(board);
            ScoreDirections moved = new MonteCarloTreeSearch().getBestMoveTime(x, 500, 1000, Math.sqrt(2));
            return new Position[] { moved.PP.from, moved.PP.to };
        } catch (CloneNotSupportedException e) {
        }
        return null;
    }
    
    /**
     * Runs the Monte Carlo Tree Search under the specificed conditions.
     */
    private class MonteCarloTreeSearch {
        public Map<PositionPair, Integer> plays = new HashMap<>();
        public Map<PositionPair, Integer> wins = new HashMap<>();

        public MonteCarloTreeSearch() {
        }

        /**
        * Runs the Monte Carlo Tree Search under the specificed conditions.
        * @param board a representation of the current board state.
        * @param move the maximum 'depth' of each simulation
        * @param time the maximum allowed time for the entire process to run for
        * @param c the Upper Condifence bound of the Tree (exploitation/exploration parameter)
        *
        * @return a move based on the simulations and calculations of the MCTS
        */
        public ScoreDirections getBestMoveTime(Board board, int moves, long time, double c) {
            long startTime = System.nanoTime();
            Colour myTurn = board.getTurn();
            int games = 0;
            double maxwrate = Double.MIN_VALUE;
            while (((System.nanoTime() - startTime + 500_000L) / 1_000_000L) < time) {
                runSimulation(board, moves, c);
                games++;
            }
            ScoreDirections themove = new ScoreDirections(0.0, 0, this.plays.keySet().iterator().next());
            System.out.println("GAMES = " + games);
            //System.out.println("AVGMOVES = "+ avgmoves.stream().mapToInt(val -> val).average().orElse(0.0));
            for (PositionPair move : this.plays.keySet()) {
                double wrate = (double) this.wins.get(move) / (double) this.plays.get(move);
                if (isLegalMove(board, move.from, move.to, myTurn)) {
                    System.out.println("{ " + move.from + " -> " + move.to + " } = " + wrate + " = "+ this.wins.get(move) + " " + this.plays.get(move) + " -> " + move.turn);
                    if (wrate > maxwrate) {
                        maxwrate = wrate;
                        themove = new ScoreDirections(wrate, 0, move);
                    }
                }
            }
            return themove;
        }
        
        /**
        * Runs a simulation of one game under the specificed conditions and updates the tree.
        * @param board a representation of the current board state.
        * @param MaxMoves the maximum 'depth' of each simulation.
        * @param c the Upper Condifence bound of the Tree (exploitation/exploration parameter).
        *
        */
        public void runSimulation(Board board, int MaxMoves, double c) {
            try {
                double maxeval = Double.MIN_VALUE;
                Set<PositionPair> visited_states = new HashSet<>();
                Board cpyGame = getGame(board);
                Colour curTurn = board.getTurn();
                Colour winner = curTurn;
                Colour loser = curTurn;
                int maxscore = Integer.MIN_VALUE;
                int minscore = Integer.MAX_VALUE;
                boolean Expand = true;
                boolean playsinmove = true;
                Position[] move;
                for (int i = 0; i < MaxMoves; i++) {
                    Colour x = Colour.values()[(curTurn.ordinal()+1)%3];
                    Colour y = Colour.values()[(x.ordinal()+1)%3];
                    Map<Position[], Board> possmoves = makeLegalMoves(cpyGame, curTurn);
                    for (Position[] a : possmoves.keySet()) {
                        Set<Position> one = possmoves.get(a).getPositions(x);
                        one.addAll(possmoves.get(a).getPositions(y));
                        if (!(plays.keySet().contains(new PositionPair(a[0], a[1], curTurn, possmoves.get(a).getPositions(curTurn), one)))) {
                            playsinmove = false;
                        }
                    }
                    if (playsinmove) {
                        double eval = 0.0;
                        double log_total = 0.0;
                        for (Position[] a : possmoves.keySet()) {
                            Set<Position> one = possmoves.get(a).getPositions(x);
                            one.addAll(possmoves.get(a).getPositions(y));
                            log_total += this.plays.get(new PositionPair(a[0], a[1], curTurn, possmoves.get(a).getPositions(curTurn), one));
                        }
                        log_total = Math.log(log_total);
                        for (Position[] b : possmoves.keySet()) {
                            Set<Position> one = possmoves.get(b).getPositions(x);
                            one.addAll(possmoves.get(b).getPositions(y));
                            PositionPair cur_move = new PositionPair(b[0], b[1], curTurn, possmoves.get(b).getPositions(curTurn), one);
                            eval = (this.wins.get(cur_move) / (double) this.plays.get(cur_move))
                                    + c * (Math.sqrt(log_total / this.plays.get(cur_move)));
                            if (eval > maxeval) {
                                maxeval = eval;
                                move = new Position[] { b[0], b[1] };
                            }
                        }
                    }
                    move = (Position[]) possmoves.keySet().toArray()[random.nextInt(possmoves.size())];
                    cpyGame.move(move[0], move[1]);
                    Set<Position> one = cpyGame.getPositions(x);
                    one.addAll(cpyGame.getPositions(y));
                    PositionPair toGo = new PositionPair(move[0], move[1], curTurn, cpyGame.getPositions(curTurn), one);
                    if (Expand && !this.plays.containsKey(toGo)) {
                        Expand = false;
                        this.plays.put(toGo, 0);
                        this.wins.put(toGo, 0);
                    }

                    if (this.plays.containsKey(toGo)) {
                        visited_states.add(toGo);
                    }
                    curTurn = cpyGame.getTurn();
                    if (cpyGame.gameOver()) {
                        winner = cpyGame.getWinner();
                        loser = cpyGame.getLoser();
                        break;
                    }
                }
                if (!cpyGame.gameOver()) {
                    for (Colour a : colours) {
                        if (cpyGame.score(a) > maxscore) {
                            winner = a;
                        }
                        if(cpyGame.score(a) < minscore){
                            loser = a;
                        }
                    }
                }
                for (PositionPair moves : visited_states) {
                    if (this.plays.get(moves) != null) {
                        this.plays.put(moves, this.plays.get(moves) + 1);
                    }
                    if (winner == moves.turn && this.wins.get(moves) != null) {
                        this.wins.put(moves, this.wins.get(moves) + 1);
                    }
                    if(loser == moves.turn && this.wins.get(moves) != null){
                        this.wins.put(moves, this.wins.get(moves) - 1);
                    }
                }
            } catch (CloneNotSupportedException | ImpossiblePositionException e) {
            }
        }
    }

    /**
     * A private class used for the Priority queue
     */
    private class ScoreDirections {
        public double eval;
        public Integer score;
        public PositionPair PP;

        private ScoreDirections(Double eval, Integer score, PositionPair pp) {
            this.eval = eval;
            this.PP = pp;
            this.score = score;
        }
    }
    
    /**
     * A private class used for the MCTS Tree Search & Update
     */
    public final class PositionPair {
        public final Position from;
        public final Position to;
        public final Colour turn;
        public final Set<Position> gamestate;
        public final Set<Position> opponents;

        public PositionPair(Position from, Position to, Colour turn, Set<Position> gamestate, Set<Position> opponents) {
            this.from = from;
            this.to = to;
            this.turn = turn;
            this.gamestate = gamestate;
            this.opponents = opponents;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof PositionPair)) {
                return false;
            }
            if (o == null || o.getClass() != this.getClass()) {
                return false;
            }
            PositionPair other = (PositionPair) o;
            return other.turn == this.turn && this.gamestate.equals(other.gamestate)
                    && other.gamestate.equals(this.gamestate) && other.opponents.equals(this.opponents);
        }

        @Override
        public int hashCode() {
            return 71 * gamestate.hashCode() + turn.hashCode() + opponents.hashCode();
        }
    }

    /**
     * Generates a set of Position arrays which are all legal moves in the current
     * state of the game board.
     * 
     * @param board The representation of the game state.
     * @return a set of Position arrays {start, end} which are all legal moves in
     *         the current game state.
     */
    public Map<Position[], Board> makeLegalMoves(Board board, Colour turn) {
        Map<Position[], Board> legalMoves = new HashMap<>();
        try {
            Board cpyGame = getGame(board);
            Position[] pieces = board.getPositions(turn).toArray(new Position[0]);
            for (Position square : pieces) {
                Position end = pieces[0];
                Piece mover = board.getPiece(square);
                Direction[][] steps = mover.getType().getSteps();
                int reps = mover.getType().getStepReps();
                end = square;
                for (Direction[] step : steps) {
                    end = square;
                    for (int i = 0; i < reps; i++) {
                        try {
                            if (isLegalMove(board, square, board.step(mover, step, end), turn)) {
                                end = board.step(mover, step, end);
                                cpyGame.move(square, end);
                                legalMoves.put(new Position[] { square, end }, cpyGame);
                                cpyGame = getGame(board);
                            }
                        } catch (ImpossiblePositionException e) {
                        }
                    }
                }
            }
        } catch (CloneNotSupportedException e1) {
        }
        
        return legalMoves;
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
     * Displays the final board position to the agent, 
     * if required for learning purposes. 
     * Other a default implementation may be given.
     * @param finalBoard the end position of the board
     * **/
    public void finalBoard(Board finalBoard){
    }

    /**
    * Checks if a move is legal. 
    * The move is specified by the start position (where the moving piece begins),
    * and the end position, where the piece intends to move to.
    * The conditions checked are: 
    * there is a piece at the start position; 
    * the colour of that piece correspond to the player whose turn it is;
    * if there is a piece at the end position, it cannot be the same as the moving piece;
    * the moving piece must be executing one or more steps allowed for their type, including
    * two steps forward for initial pawn moves and castling left and right;
    * pieces that can make iterated moves must iterate a single step type and cannot pass through any other piece.
    * Note, en passant is not allowed, you can castle after King or rook have moved 
    * but they must have returned to their initial position, all pawns reaching the back row are promoted to Queen,
    * you may move into check, and you may leave your king in check, and you may castle across check.
    * 
    * Works the same as the function in Board.java except is applicable to each player for lookahead.
    * 
    * @param board the representation of the gamestate.
    * @param start the starting position of the piece.
    * @param end the end position the piece intends to move to.
    * @param turn the turn of the players move being tested.
    * @return true if and only if the move is legal in the rules of the game.
    * **/
    public boolean isLegalMove(Board board, Position start, Position end, Colour turn){
        Piece mover = board.getPiece(start);
        Piece target = board.getPiece(end);
        if(mover==null) return false;//you must move a piece
        Colour mCol =mover.getColour();
        if(mCol!=turn) return false;//it must be your turn
        if(target!= null && mCol==target.getColour())return false; //you can't take your own piece
        Direction[][] steps = mover.getType().getSteps();
        switch(mover.getType()){
          case PAWN://note, there is no two step first move
            for(int i = 0; i<steps.length; i++){
              try{
                if(end == board.step(mover,steps[i],start) && 
                    ((target==null && i==0) // 1 step forward, not taking
                     || (target==null && i==1 // 2 steps forward, 
                       && start.getColour()==mCol && start.getRow()==1 //must be in initial position
                       && board.getPiece(Position.get(mCol,2,start.getColumn()))==null)//and can't jump a piece 
                     || (target!=null && i>1)//or taking diagonally
                    )
                  )
                  return true;
              }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
            }
            break;
          case KNIGHT:
            for(int i = 0; i<steps.length; i++){
              try{
                if(end == board.step(mover, steps[i],start))
                  return true;
              }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
            }
            break;
          case KING://note, you can move into check or remain in check. You may also castle across check
            for(int i = 0; i<steps.length; i++){
              try{
                if(end == board.step(mover, steps[i],start))
                  return true;
              }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
            }
            //castling: Must have king and rook in their original positions, although they may have moved
            try{
              if(start==Position.get(mCol,0,4)){
                if(end==Position.get(mCol,0,6)){
                  Piece castle = board.getPiece(Position.get(mCol,0,7));
                  Piece empty1 = board.getPiece(Position.get(mCol,0,5));
                  Piece empty2 = board.getPiece(Position.get(mCol,0,6));
                  if(castle.getType()==PieceType.ROOK && castle.getColour()==mover.getColour() && empty1==null && empty2==null)
                    return true;
                }
                if(end==Position.get(mCol,0,2)){
                  Piece castle = board.getPiece(Position.get(mCol,0,0));
                  Piece empty1 = board.getPiece(Position.get(mCol,0,1));
                  Piece empty2 = board.getPiece(Position.get(mCol,0,2));
                  Piece empty3 = board.getPiece(Position.get(mCol,0,3));
                  if(castle.getType()==PieceType.ROOK && castle.getColour()==mover.getColour() && empty1==null && empty2==null && empty3==null)
                    return true;
                }
              }
            }catch(ImpossiblePositionException e){}//do nothing, all positions possible here.
            break;
          default://rook, bishop, queen, just need to check that one of their steps is iterated.
            for(int i = 0; i<steps.length; i++){
              Direction[] step = steps[i];
              try{
                Position tmp = board.step(mover,step,start);
                while(end != tmp && board.getPiece(tmp)==null){
                  if(tmp.getColour()!=start.getColour()){//flip steps when moving between board sections.
                    step = new Direction[steps[i].length];
                    for(int j = 0; j<steps[i].length; j++){
                      switch(steps[i][j]){
                        case FORWARD: step[j] = Direction.BACKWARD; break;
                        case BACKWARD: step[j] = Direction.FORWARD; break;
                        case LEFT: step[j] = Direction.RIGHT; break;
                        case RIGHT: step[j] = Direction.LEFT; break;
                      }
                    }
                  }
                  tmp = board.step(mover, step,tmp);
                }
                if(end==tmp) return true;
              }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
            }
            break;
        }
        return false;//move did not match any legal option.
    }
    
    /**
     * @return the Agent's name, for annotating game description.
     * **/ 
    public String toString(){
        return name;
    }

}