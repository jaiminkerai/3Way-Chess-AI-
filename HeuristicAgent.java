package threeChess.agents;

import java.util.*;
import threeChess.*;

public class SmartAgent extends Agent {

    private String name = "SmartAgent";
    private final Direction[] neighbours = { Direction.FORWARD, Direction.BACKWARD, Direction.LEFT, Direction.RIGHT };

    /**
     * A no argument constructor, required for tournament management.
     **/
    public SmartAgent(){
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
        Set<Position[]> MylegalMoves = makeLegalMoves(board, board.getTurn());
        Double maxeval = Double.MIN_VALUE;
        double eval;
        Position[] move = MylegalMoves.iterator().next();
        Colour myTurn = board.getTurn();
        try {
            for (Position[] moves : MylegalMoves) {
                Board cpyGame = getGame(board);
                cpyGame.move(moves[0], moves[1]);
                if (cpyGame.getWinner() == myTurn) {
                    return new Position[] { moves[0], moves[1] };
                }
                Set<Position[]> temp_legalMoves = makeLegalMoves(cpyGame, myTurn);
                eval = evaluate(cpyGame, myTurn, temp_legalMoves.size(),
                        isCheck(cpyGame, moves[1], moves[0], myTurn, 1),
                        isCheck(cpyGame, moves[1], moves[0], myTurn, 2), isCheck(board, moves[1], moves[0], myTurn, 3));
                // System.out.println(moves[0]+" -> "+moves[1]+" = "+eval);
                if (eval > maxeval) {
                    maxeval = eval;
                    move = new Position[] { moves[0], moves[1] };
                }
            }
        } catch (CloneNotSupportedException | ImpossiblePositionException e) {
        }
        return move;
    }

    /**
     * Returns the value of a calculated heuristic based on the given 't' value.
     * 
     * t = 1: determines if the current players piece is under threat of capture
     * after the move.
     * t = 2: determines if the current player puts any opponents pieces under
     * threat of capture after the move.
     * t = 3: determines if the current players pieces is under threat of capture.
     * t = 4: determines protection of the king after the move.
     * 
     * @param board    The representation of the game state.
     * @param end      The end position of the current piece.
     * @param original The starting position of the current piece.
     * @param turn     The turn of the current player.
     * @param int      The value determining which heuristic to calculate.
     * 
     * @return a value based on the selected heuristic.
     */
    public int isCheck(Board board, Position end, Position original, Colour turn, int t) {
        Colour MyTurn = turn;
        int CurVal = 0;
        if (t == 1) {
            Set<Position> myPieces = board.getPositions(turn);
            for (int i = 0; i < 2; i++) {
                turn = Colour.values()[(turn.ordinal() + 1) % 3];
                for (Position[] moves : makeLegalMoves(board, turn)) {
                    if (moves[1] == end && -board.getPiece(moves[1]).getValue() < CurVal) {
                        CurVal = -board.getPiece(end).getValue();
                    }
                    if (myPieces.contains(moves[1]) && -board.getPiece(moves[1]).getValue() < CurVal) {
                        CurVal = -board.getPiece(moves[1]).getValue();
                    }
                }
            }
        } else if (t == 2) {
            for (Position[] moves : makeLegalMoves(board, MyTurn)) {
                if (moves[0] == end) {
                    for (int i = 0; i < 2; i++) {
                        turn = Colour.values()[(turn.ordinal() + 1) % 3];
                        Set<Position> pieces = board.getPositions(turn);
                        if (pieces.contains(moves[1]) && board.getPiece(moves[1]).getValue() > CurVal) {
                            CurVal = board.getPiece(moves[1]).getValue();
                        }
                    }
                }
            }
            return CurVal;
        } else if (t == 3) {
            for (int i = 0; i < 2; i++) {
                turn = Colour.values()[(turn.ordinal() + 1) % 3];
                for (Position[] moves : makeLegalMoves(board, turn)) {
                    if (moves[1] == original) {
                        return board.getPiece(original).getValue();
                    }
                }
            }
        } else if (t == 4) {
            Set<Position> pn = board.getPositions(turn);
            Position x = pn.iterator().next();
            for (Position p : pn) {
                if (board.getPiece(p).getType() == PieceType.KING) {
                    x = p;
                }
            }
            for (Direction[] steps : PieceType.KING.getSteps()) {
                try {
                    if (pn.contains(board.step(board.getPiece(x), steps, x))) {
                        CurVal += board.getPiece(board.step(board.getPiece(x), steps, x)).getValue();
                    }
                } catch (ImpossiblePositionException e) {
                }
            }
        }
        return CurVal;
    }

    /**
     * Generates a set of Position arrays which are all legal moves in the current
     * state of the game board.
     * 
     * @param board The representation of the game state.
     * @return a set of Position arrays {start, end} which are all legal moves in
     *         the current game state.
     */
    public Set<Position[]> makeLegalMoves(Board board, Colour turn) {
        Set<Position[]> legalMoves = new HashSet<>();
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
                        Position x = board.step(mover, step, end);
                        if (isLegalMove(board, square, x, turn)) {
                            end = x;
                            legalMoves.add(new Position[] { square, end });
                        }
                    } catch (ImpossiblePositionException e) {
                    }
                }
            }
        }
        return legalMoves;
    }

    /**
     * Generates the number of all legal moves in the current
     * state of the game board.
     * 
     * @param board The representation of the game state.
     * @return a set of Position arrays {start, end} which are all legal moves in
     *         the current game state.
     */
    public int numLegalMoves(Board board, Colour turn) {
        int count = 0;
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
                        Position x = board.step(mover, step, end);
                        if (isLegalMove(board, square, x, turn)) {
                            end = x;
                            count++;
                        }
                    } catch (ImpossiblePositionException e) {
                    }
                }
            }
        }
        return count;
    }

    /**
     * A function which evalutes a move based on a formula.
     * 
     * @param board     The representation of the game state.
     * @param myPlayer  The colour representation of whose pieces are being
     *                  calculated.
     * @param moves     Number of possible moves of our player.
     * @param checkNum  the greatest worth piece under threat of capture, given the
     *                  move.
     * @param OppCheck  the greatest worth opponent piece under thret of after,
     *                  given the move.
     * @param CurrCheck the greatest worth piece currently under threat of capture.
     * @return a double representation of the evaluation of the move.
     * 
     */
    public double evaluate(Board board, Colour myPlayer, int moves, int checkNum, int OppCheck, int CurrCheck) {
        // 1.5, 1, 1
        // 2.5, 0.25, 1.5
        double score = 2.65 * checkNum + 0.25 * OppCheck + 1.75 * CurrCheck;
        Integer MynumOfLegalMoves = moves;
        Map<PieceType, Integer> MyPieceCount = countPieces(board, myPlayer);
        Map<String, Integer> MyPawns = countPawns(board, PiecePos(PieceType.PAWN, board, myPlayer));
        Colour turn = myPlayer;
        for (int i = 0; i < 2; i++) {
            turn = Colour.values()[(turn.ordinal() + 1) % 3];
            Map<PieceType, Integer> PieceCount = countPieces(board, turn);
            Map<String, Integer> Pawns = countPawns(board, PiecePos(PieceType.PAWN, board, turn));
            int numOfLegalMoves = numLegalMoves(board, turn);
            score += 500 * (MyPieceCount.get(PieceType.KING) - PieceCount.get(PieceType.KING))
                    + 9 * (MyPieceCount.get(PieceType.QUEEN) - PieceCount.get(PieceType.QUEEN))
                    + 5 * (MyPieceCount.get(PieceType.ROOK) - PieceCount.get(PieceType.ROOK))
                    + 3 * ((MyPieceCount.get(PieceType.BISHOP) - PieceCount.get(PieceType.BISHOP))
                            + (MyPieceCount.get(PieceType.KNIGHT) - PieceCount.get(PieceType.KNIGHT)))
                    + 1 * (MyPieceCount.get(PieceType.PAWN) - PieceCount.get(PieceType.PAWN))
                    - 0.5 * ((MyPawns.get("doubled") - Pawns.get("doubled"))
                            + (MyPawns.get("blocked") - Pawns.get("blocked"))
                            + (MyPawns.get("isolated") - Pawns.get("isolated")))
                    + 0.1 * (MynumOfLegalMoves - numOfLegalMoves);
        }
        return score;
    }

    /**
     * A function which calculates the number of pieces for each type of piece.
     * 
     * @param board The representation of the game state.
     * @param turn  The colour representation of whose pieces are being calculated.
     * @return A map (dictionary) containing the values of each of the pieces of a
     *         game state.
     */
    public Map<PieceType, Integer> countPieces(Board board, Colour turn) {
        Map<PieceType, Integer> countOfPieces = new EnumMap<>(PieceType.class);
        Position[] pieces = board.getPositions(turn).toArray(new Position[0]);
        countOfPieces.put(PieceType.BISHOP, 0);
        countOfPieces.put(PieceType.KING, 0);
        countOfPieces.put(PieceType.KNIGHT, 0);
        countOfPieces.put(PieceType.PAWN, 0);
        countOfPieces.put(PieceType.QUEEN, 0);
        countOfPieces.put(PieceType.ROOK, 0);
        for (Position square : pieces) {
            PieceType mover = board.getPiece(square).getType();
            countOfPieces.put(mover, countOfPieces.get(mover) + 1);
        }
        return countOfPieces;
    }

    /**
     * A function which calculates the positions for a given type of piece.
     * 
     * @param mover The type of piece.
     * @param board The representation of the game state.
     * @param turn  The colour representation of whose pieces are being calculated.
     * @return A set containing all the positions for a given type of piece.
     */
    public Set<Position> PiecePos(PieceType mover, Board board, Colour turn) {
        Set<Position> end = new HashSet<>();
        Position[] pieces = board.getPositions(turn).toArray(new Position[0]);
        for (Position piece : pieces) {
            if (board.getPiece(piece).getType() == mover) {
                end.add(piece);
            }
        }
        return end;
    }

    /**
     * A function which calculates the number of blocked, isolated and doubled
     * pawns.
     * 
     * @param board     The representation of the game state.
     * @param legalMove A set of legal moves given the board state.
     * @return A map (dictionary) containing the values for the pawns of a game
     *         state (blocked, isolated and doubled).
     */
    public Map<String, Integer> countPawns(Board board, Set<Position> legalMove) {
        Map<String, Integer> countOfPawns = new HashMap<>();
        countOfPawns.put("doubled", 0);
        countOfPawns.put("isolated", 0);
        countOfPawns.put("blocked", 0);
        for (Position piece : legalMove) {
            int countDoubled = 0;
            int countBlocked = 0;
            Position start = piece;
            for (int i = 0; i < 8; i++) {
                try {
                    start = start.neighbour(neighbours[0]);
                    if (board.getPiece(start) == null) {
                        continue;
                    }
                    if (board.getPiece(start).getType() == PieceType.PAWN
                            && board.getTurn() == board.getPiece(start).getColour()) {
                        countDoubled++;
                    }
                    if (i == piece.getColumn() + 1 && board.getPiece(start) != null) {
                        countBlocked++;
                    }
                } catch (ImpossiblePositionException e) {
                }
            }
            if (countDoubled > 1) {
                countOfPawns.put("doubled", countOfPawns.get("doubled") + 1);
            }
            if (countBlocked > 0) {
                countOfPawns.put("blocked", countOfPawns.get("blocked") + 1);
            }
            if (countDoubled == 0) {
                countOfPawns.put("isolated", countOfPawns.get("isolated") + 1);
            }
        }
        return countOfPawns;
    }

    /**
     * A function which calculates the score on a specific move.
     * 
     * @param board The representation of the game state.
     * @param start The starting Position of the piece
     * @param end   The ending Position of the piece
     * @return the score increase/decrease due to a move.
     */
    public int scoreOnMove(Board board, Position start, Position end) {
        int score = 0;
        Board cpyGame;
        int before = board.score(board.getTurn());
        try {
            cpyGame = getGame(board);
            cpyGame.move(start, end);
            score += -before + cpyGame.score(board.getTurn());

        } catch (CloneNotSupportedException | ImpossiblePositionException e) {
        }
        if (score == 0 && board.getPiece(start).getType() == PieceType.PAWN) {
            return start.getColumn();
        }
        return score;
    }

    /**
     * Returns a deep clone of the board state,
     * such that no operations will affect the original board instance.
     * 
     * @return a deep clone of the board state casted to a board Object type.
     **/
    public Board getGame(Board board) throws CloneNotSupportedException {
        return (Board) board.clone();
    }

    /**
     * @return the Agent's name, for annotating game description.
     **/
    public String toString() {
        return name;
    }

    /**
     * Displays the final board position to the agent,
     * if required for learning purposes.
     * Other a default implementation may be given.
     * 
     * @param finalBoard the end position of the board
     **/
    public void finalBoard(Board finalBoard) {
    }

    /**
     * Checks if a move is legal.
     * The move is specified by the start position (where the moving piece begins),
     * and the end position, where the piece intends to move to.
     * The conditions checked are:
     * there is a piece at the start position;
     * the colour of that piece correspond to the player whose turn it is;
     * if there is a piece at the end position, it cannot be the same as the moving
     * piece;
     * the moving piece must be executing one or more steps allowed for their type,
     * including
     * two steps forward for initial pawn moves and castling left and right;
     * pieces that can make iterated moves must iterate a single step type and
     * cannot pass through any other piece.
     * Note, en passant is not allowed, you can castle after King or rook have moved
     * but they must have returned to their initial position, all pawns reaching the
     * back row are promoted to Queen,
     * you may move into check, and you may leave your king in check, and you may
     * castle across check.
     * 
     * Works the same as the function in Board.java except is applicable to each
     * player for lookahead.
     * 
     * @param board the representation of the gamestate.
     * @param start the starting position of the piece.
     * @param end   the end position the piece intends to move to.
     * @param turn  the turn of the players move being tested.
     * @return true if and only if the move is legal in the rules of the game.
     **/
    public boolean isLegalMove(Board board, Position start, Position end, Colour turn) {
        Piece mover = board.getPiece(start);
        Piece target = board.getPiece(end);
        if (mover == null)
            return false;// you must move a piece
        Colour mCol = mover.getColour();
        if (mCol != turn)
            return false;// it must be your turn
        if (target != null && mCol == target.getColour())
            return false; // you can't take your own piece
        Direction[][] steps = mover.getType().getSteps();
        switch (mover.getType()) {
            case PAWN:// note, there is no two step first move
                for (int i = 0; i < steps.length; i++) {
                    try {
                        if (end == board.step(mover, steps[i], start) &&
                                ((target == null && i == 0) // 1 step forward, not taking
                                        || (target == null && i == 1 // 2 steps forward,
                                                && start.getColour() == mCol && start.getRow() == 1 // must be in
                                                                                                    // initial position
                                                && board.getPiece(Position.get(mCol, 2, start.getColumn())) == null)// and
                                                                                                                    // can't
                                                                                                                    // jump
                                                                                                                    // a
                                                                                                                    // piece
                                        || (target != null && i > 1)// or taking diagonally
                                ))
                            return true;
                    } catch (ImpossiblePositionException e) {
                    } // do nothing, steps went off board.
                }
                break;
            case KNIGHT:
                for (int i = 0; i < steps.length; i++) {
                    try {
                        if (end == board.step(mover, steps[i], start))
                            return true;
                    } catch (ImpossiblePositionException e) {
                    } // do nothing, steps went off board.
                }
                break;
            case KING:// note, you can move into check or remain in check. You may also castle across
                      // check
                for (int i = 0; i < steps.length; i++) {
                    try {
                        if (end == board.step(mover, steps[i], start))
                            return true;
                    } catch (ImpossiblePositionException e) {
                    } // do nothing, steps went off board.
                }
                // castling: Must have king and rook in their original positions, although they
                // may have moved
                try {
                    if (start == Position.get(mCol, 0, 4)) {
                        if (end == Position.get(mCol, 0, 6)) {
                            Piece castle = board.getPiece(Position.get(mCol, 0, 7));
                            Piece empty1 = board.getPiece(Position.get(mCol, 0, 5));
                            Piece empty2 = board.getPiece(Position.get(mCol, 0, 6));
                            if (castle.getType() == PieceType.ROOK && castle.getColour() == mover.getColour()
                                    && empty1 == null && empty2 == null)
                                return true;
                        }
                        if (end == Position.get(mCol, 0, 2)) {
                            Piece castle = board.getPiece(Position.get(mCol, 0, 0));
                            Piece empty1 = board.getPiece(Position.get(mCol, 0, 1));
                            Piece empty2 = board.getPiece(Position.get(mCol, 0, 2));
                            Piece empty3 = board.getPiece(Position.get(mCol, 0, 3));
                            if (castle.getType() == PieceType.ROOK && castle.getColour() == mover.getColour()
                                    && empty1 == null && empty2 == null && empty3 == null)
                                return true;
                        }
                    }
                } catch (ImpossiblePositionException e) {
                } // do nothing, all positions possible here.
                break;
            default:// rook, bishop, queen, just need to check that one of their steps is iterated.
                for (int i = 0; i < steps.length; i++) {
                    Direction[] step = steps[i];
                    try {
                        Position tmp = board.step(mover, step, start);
                        while (end != tmp && board.getPiece(tmp) == null) {
                            if (tmp.getColour() != start.getColour()) {// flip steps when moving between board sections.
                                step = new Direction[steps[i].length];
                                for (int j = 0; j < steps[i].length; j++) {
                                    switch (steps[i][j]) {
                                        case FORWARD:
                                            step[j] = Direction.BACKWARD;
                                            break;
                                        case BACKWARD:
                                            step[j] = Direction.FORWARD;
                                            break;
                                        case LEFT:
                                            step[j] = Direction.RIGHT;
                                            break;
                                        case RIGHT:
                                            step[j] = Direction.LEFT;
                                            break;
                                    }
                                }
                            }
                            tmp = board.step(mover, step, tmp);
                        }
                        if (end == tmp)
                            return true;
                    } catch (ImpossiblePositionException e) {
                    } // do nothing, steps went off board.
                }
                break;
        }
        return false;// move did not match any legal option.
    }

}
