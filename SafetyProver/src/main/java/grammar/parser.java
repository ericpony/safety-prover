
//----------------------------------------------------
// The following code was generated by CUP v0.10k
// Tue Dec 27 21:46:24 CST 2016
//----------------------------------------------------

package grammar;


/** CUP v0.10k generated parser.
  * @version Tue Dec 27 21:46:24 CST 2016
  */
public class parser extends java_cup.runtime.lr_parser {

  /** Default constructor. */
  public parser() {super();}

  /** Constructor which sets the default scanner. */
  public parser(java_cup.runtime.Scanner s) {super(s);}

  /** Production table. */
  protected static final short _production_table[][] = 
    unpackFromStrings(new String[] {
    "\000\053\000\002\002\004\000\002\003\012\000\002\004" +
    "\007\000\002\005\006\000\002\006\010\000\002\006\005" +
    "\000\002\006\005\000\002\007\006\000\002\010\007\000" +
    "\002\011\006\000\002\012\006\000\002\012\005\000\002" +
    "\013\006\000\002\014\007\000\002\014\007\000\002\014" +
    "\007\000\002\014\005\000\002\014\005\000\002\014\003" +
    "\000\002\014\003\000\002\014\003\000\002\014\005\000" +
    "\002\014\005\000\002\015\002\000\002\015\005\000\002" +
    "\016\003\000\002\016\006\000\002\017\002\000\002\017" +
    "\003\000\002\017\005\000\002\020\004\000\002\020\002" +
    "\000\002\021\003\000\002\021\003\000\002\022\002\000" +
    "\002\022\003\000\002\022\005\000\002\023\002\000\002" +
    "\023\003\000\002\023\005\000\002\024\002\000\002\024" +
    "\003\000\002\024\005" });

  /** Access to production table. */
  public short[][] production_table() {return _production_table;}

  /** Parse-action table. */
  protected static final short[][] _action_table = 
    unpackFromStrings(new String[] {
    "\000\147\000\004\015\005\001\002\000\004\002\151\001" +
    "\002\000\004\004\007\001\002\000\006\016\uffe2\021\041" +
    "\001\002\000\004\023\010\001\002\000\004\006\035\001" +
    "\002\000\010\017\uffdc\037\016\040\014\001\002\000\004" +
    "\010\032\001\002\000\006\007\030\017\uffdb\001\002\000" +
    "\022\005\uffe1\007\uffe1\010\uffe1\011\uffe1\013\uffe1\017\uffe1" +
    "\037\uffe1\040\uffe1\001\002\000\004\017\020\001\002\000" +
    "\022\005\uffe0\007\uffe0\010\uffe0\011\uffe0\013\uffe0\017\uffe0" +
    "\037\uffe0\040\uffe0\001\002\000\004\005\027\001\002\000" +
    "\004\006\021\001\002\000\010\007\uffd9\037\016\040\014" +
    "\001\002\000\012\005\uffd8\007\uffd8\013\025\017\uffd8\001" +
    "\002\000\004\007\024\001\002\000\004\005\ufff5\001\002" +
    "\000\014\005\uffd9\007\uffd9\017\uffd9\037\016\040\014\001" +
    "\002\000\010\005\uffd7\007\uffd7\017\uffd7\001\002\000\034" +
    "\002\ufff9\016\ufff9\020\ufff9\021\ufff9\022\ufff9\024\ufff9\025" +
    "\ufff9\027\ufff9\030\ufff9\031\ufff9\034\ufff9\035\ufff9\036\ufff9" +
    "\001\002\000\010\017\uffdc\037\016\040\014\001\002\000" +
    "\004\017\uffda\001\002\000\006\037\016\040\014\001\002" +
    "\000\012\007\ufff6\017\ufff6\037\016\040\014\001\002\000" +
    "\006\007\ufff7\017\ufff7\001\002\000\006\037\016\040\014" +
    "\001\002\000\004\007\037\001\002\000\010\017\ufff8\037" +
    "\ufff8\040\ufff8\001\002\000\004\016\043\001\002\000\004" +
    "\007\042\001\002\000\004\016\uffe3\001\002\000\004\004" +
    "\045\001\002\000\004\014\076\001\002\000\004\023\047" +
    "\001\002\000\012\017\uffdf\026\054\037\016\040\014\001" +
    "\002\000\004\006\050\001\002\000\006\037\016\040\014" +
    "\001\002\000\004\007\052\001\002\000\012\017\ufffe\026" +
    "\ufffe\037\ufffe\040\ufffe\001\002\000\004\010\071\001\002" +
    "\000\004\006\067\001\002\000\006\007\065\017\uffde\001" +
    "\002\000\004\017\060\001\002\000\004\005\064\001\002" +
    "\000\004\006\061\001\002\000\010\007\uffd9\037\016\040" +
    "\014\001\002\000\004\007\063\001\002\000\004\005\ufffa" +
    "\001\002\000\004\014\uffff\001\002\000\012\017\uffdf\026" +
    "\054\037\016\040\014\001\002\000\004\017\uffdd\001\002" +
    "\000\012\007\uffd9\017\uffd9\037\016\040\014\001\002\000" +
    "\006\007\ufffb\017\ufffb\001\002\000\006\037\016\040\014" +
    "\001\002\000\012\007\ufffc\017\ufffc\037\016\040\014\001" +
    "\002\000\004\011\074\001\002\000\006\037\016\040\014" +
    "\001\002\000\006\007\ufffd\017\ufffd\001\002\000\004\004" +
    "\007\001\002\000\030\002\uffea\020\uffea\022\uffea\024\uffea" +
    "\025\uffea\027\uffea\030\uffea\031\uffea\034\uffea\035\uffea\036" +
    "\uffea\001\002\000\030\002\000\020\112\022\111\024\110" +
    "\025\107\027\104\030\106\031\105\034\103\035\102\036" +
    "\101\001\002\000\004\007\uffef\001\002\000\004\006\145" +
    "\001\002\000\004\006\133\001\002\000\004\007\uffee\001" +
    "\002\000\004\006\131\001\002\000\004\007\uffed\001\002" +
    "\000\004\006\127\001\002\000\004\006\123\001\002\000" +
    "\004\006\121\001\002\000\004\006\115\001\002\000\004" +
    "\007\114\001\002\000\030\002\uffe9\020\uffe9\022\uffe9\024" +
    "\uffe9\025\uffe9\027\uffe9\030\uffe9\031\uffe9\034\uffe9\035\uffe9" +
    "\036\uffe9\001\002\000\004\040\116\001\002\000\004\012" +
    "\117\001\002\000\004\040\120\001\002\000\004\007\ufff3" +
    "\001\002\000\004\040\122\001\002\000\004\007\ufff0\001" +
    "\002\000\004\040\124\001\002\000\004\012\125\001\002" +
    "\000\004\040\126\001\002\000\004\007\ufff2\001\002\000" +
    "\004\040\130\001\002\000\004\007\uffec\001\002\000\004" +
    "\040\132\001\002\000\004\007\uffeb\001\002\000\010\007" +
    "\uffe6\032\134\033\137\001\002\000\006\007\uffe8\013\uffe8" +
    "\001\002\000\006\007\uffe5\013\143\001\002\000\004\007" +
    "\ufff1\001\002\000\004\004\140\001\002\000\010\005\uffd9" +
    "\037\016\040\014\001\002\000\004\005\142\001\002\000" +
    "\006\007\uffe7\013\uffe7\001\002\000\010\007\uffe6\032\134" +
    "\033\137\001\002\000\004\007\uffe4\001\002\000\004\040" +
    "\146\001\002\000\004\012\147\001\002\000\004\040\150" +
    "\001\002\000\004\007\ufff4\001\002\000\004\002\001\001" +
    "\002" });

  /** Access to parse-action table. */
  public short[][] action_table() {return _action_table;}

  /** <code>reduce_goto</code> table. */
  protected static final short[][] _reduce_table = 
    unpackFromStrings(new String[] {
    "\000\147\000\004\003\003\001\001\000\002\001\001\000" +
    "\004\010\005\001\001\000\004\020\037\001\001\000\004" +
    "\011\010\001\001\000\002\001\001\000\010\012\012\021" +
    "\011\023\014\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\004\013\016\001\001\000\002\001" +
    "\001\000\002\001\001\000\002\001\001\000\006\021\021" +
    "\024\022\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\006\021\021\024\025\001\001\000\002" +
    "\001\001\000\002\001\001\000\010\012\012\021\011\023" +
    "\030\001\001\000\002\001\001\000\004\021\032\001\001" +
    "\000\004\021\033\001\001\000\002\001\001\000\004\021" +
    "\035\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001\000\004\004" +
    "\043\001\001\000\002\001\001\000\004\005\045\001\001" +
    "\000\010\006\054\021\052\022\055\001\001\000\002\001" +
    "\001\000\004\021\050\001\001\000\002\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
    "\001\000\004\007\056\001\001\000\002\001\001\000\002" +
    "\001\001\000\006\021\021\024\061\001\001\000\002\001" +
    "\001\000\002\001\001\000\002\001\001\000\010\006\054" +
    "\021\052\022\065\001\001\000\002\001\001\000\006\021" +
    "\021\024\067\001\001\000\002\001\001\000\004\021\071" +
    "\001\001\000\004\021\072\001\001\000\002\001\001\000" +
    "\004\021\074\001\001\000\002\001\001\000\004\010\076" +
    "\001\001\000\004\015\077\001\001\000\004\014\112\001" +
    "\001\000\002\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
    "\001\000\002\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
    "\001\000\006\016\134\017\135\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\006\021\021\024\140\001\001\000\002\001\001\000\002" +
    "\001\001\000\006\016\134\017\143\001\001\000\002\001" +
    "\001\000\002\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001" });

  /** Access to <code>reduce_goto</code> table. */
  public short[][] reduce_table() {return _reduce_table;}

  /** Instance of action encapsulation class. */
  protected CUP$parser$actions action_obj;

  /** Action encapsulation object initializer. */
  protected void init_actions()
    {
      action_obj = new CUP$parser$actions(this);
    }

  /** Invoke a user supplied parse action. */
  public java_cup.runtime.Symbol do_action(
    int                        act_num,
    java_cup.runtime.lr_parser parser,
    java.util.Stack            stack,
    int                        top)
    throws java.lang.Exception
  {
    /* call code in generated class */
    return action_obj.CUP$parser$do_action(act_num, parser, stack, top);
  }

  /** Indicates start state. */
  public int start_state() {return 0;}
  /** Indicates start production. */
  public int start_production() {return 0;}

  /** <code>EOF</code> Symbol index. */
  public int EOF_sym() {return 0;}

  /** <code>error</code> Symbol index. */
  public int error_sym() {return 1;}



  public grammar.Absyn.ModelRule pModelRule() throws Exception
  {
	java_cup.runtime.Symbol res = parse();
	return (grammar.Absyn.ModelRule) res.value;
  }

public <B,A extends java.util.LinkedList<? super B>> A cons_(B x, A xs) { xs.addFirst(x); return xs; }

public void syntax_error(java_cup.runtime.Symbol cur_token)
{
	report_error("Syntax Error, trying to recover and continue parse...", cur_token);
}

public void unrecovered_syntax_error(java_cup.runtime.Symbol cur_token) throws java.lang.Exception
{
	throw new Exception("Unrecoverable Syntax Error");
}


}

/** Cup generated class to encapsulate user supplied action code.*/
class CUP$parser$actions {
  private final parser parser;

  /** Constructor */
  CUP$parser$actions(parser parser) {
    this.parser = parser;
  }

  /** Method with the actual generated action code. */
  public final java_cup.runtime.Symbol CUP$parser$do_action(
    int                        CUP$parser$act_num,
    java_cup.runtime.lr_parser CUP$parser$parser,
    java.util.Stack            CUP$parser$stack,
    int                        CUP$parser$top)
    throws java.lang.Exception
    {
      /* Symbol object for return from actions */
      java_cup.runtime.Symbol CUP$parser$result;

      /* select the action based on the action number */
      switch (CUP$parser$act_num)
        {
          /*. . . . . . . . . . . . . . . . . . . .*/
          case 42: // ListName ::= Name _SYMB_7 ListName 
            {
              grammar.Absyn.ListName RESULT = null;
		grammar.Absyn.Name p_1 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.ListName p_3 = (grammar.Absyn.ListName)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = p_3; p_3.addFirst(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(18/*ListName*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 41: // ListName ::= Name 
            {
              grammar.Absyn.ListName RESULT = null;
		grammar.Absyn.Name p_1 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.ListName(); RESULT.addLast(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(18/*ListName*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 40: // ListName ::= 
            {
              grammar.Absyn.ListName RESULT = null;
		 RESULT = new grammar.Absyn.ListName(); 
              CUP$parser$result = new java_cup.runtime.Symbol(18/*ListName*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 39: // ListAutomataTransitionRule ::= AutomataTransitionRule _SYMB_3 ListAutomataTransitionRule 
            {
              grammar.Absyn.ListAutomataTransitionRule RESULT = null;
		grammar.Absyn.AutomataTransitionRule p_1 = (grammar.Absyn.AutomataTransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.ListAutomataTransitionRule p_3 = (grammar.Absyn.ListAutomataTransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = p_3; p_3.addFirst(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(17/*ListAutomataTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 38: // ListAutomataTransitionRule ::= AutomataTransitionRule 
            {
              grammar.Absyn.ListAutomataTransitionRule RESULT = null;
		grammar.Absyn.AutomataTransitionRule p_1 = (grammar.Absyn.AutomataTransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.ListAutomataTransitionRule(); RESULT.addLast(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(17/*ListAutomataTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 37: // ListAutomataTransitionRule ::= 
            {
              grammar.Absyn.ListAutomataTransitionRule RESULT = null;
		 RESULT = new grammar.Absyn.ListAutomataTransitionRule(); 
              CUP$parser$result = new java_cup.runtime.Symbol(17/*ListAutomataTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 36: // ListTransitionRule ::= TransitionRule _SYMB_3 ListTransitionRule 
            {
              grammar.Absyn.ListTransitionRule RESULT = null;
		grammar.Absyn.TransitionRule p_1 = (grammar.Absyn.TransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.ListTransitionRule p_3 = (grammar.Absyn.ListTransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = p_3; p_3.addFirst(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(16/*ListTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 35: // ListTransitionRule ::= TransitionRule 
            {
              grammar.Absyn.ListTransitionRule RESULT = null;
		grammar.Absyn.TransitionRule p_1 = (grammar.Absyn.TransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.ListTransitionRule(); RESULT.addLast(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(16/*ListTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 34: // ListTransitionRule ::= 
            {
              grammar.Absyn.ListTransitionRule RESULT = null;
		 RESULT = new grammar.Absyn.ListTransitionRule(); 
              CUP$parser$result = new java_cup.runtime.Symbol(16/*ListTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 33: // Name ::= LabelIdent 
            {
              grammar.Absyn.Name RESULT = null;
		String p_1 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.LiteralName(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(15/*Name*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 32: // Name ::= MyInteger 
            {
              grammar.Absyn.Name RESULT = null;
		String p_1 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.NumberName(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(15/*Name*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 31: // MaybeClosed ::= 
            {
              grammar.Absyn.MaybeClosed RESULT = null;
		 RESULT = new grammar.Absyn.NotClosedInit(); 
              CUP$parser$result = new java_cup.runtime.Symbol(14/*MaybeClosed*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 30: // MaybeClosed ::= _SYMB_13 _SYMB_3 
            {
              grammar.Absyn.MaybeClosed RESULT = null;
		 RESULT = new grammar.Absyn.ClosedInit(); 
              CUP$parser$result = new java_cup.runtime.Symbol(14/*MaybeClosed*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 29: // ListSymmetryOption ::= SymmetryOption _SYMB_7 ListSymmetryOption 
            {
              grammar.Absyn.ListSymmetryOption RESULT = null;
		grammar.Absyn.SymmetryOption p_1 = (grammar.Absyn.SymmetryOption)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.ListSymmetryOption p_3 = (grammar.Absyn.ListSymmetryOption)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = p_3; p_3.addFirst(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(13/*ListSymmetryOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 28: // ListSymmetryOption ::= SymmetryOption 
            {
              grammar.Absyn.ListSymmetryOption RESULT = null;
		grammar.Absyn.SymmetryOption p_1 = (grammar.Absyn.SymmetryOption)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.ListSymmetryOption(); RESULT.addLast(p_1); 
              CUP$parser$result = new java_cup.runtime.Symbol(13/*ListSymmetryOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 27: // ListSymmetryOption ::= 
            {
              grammar.Absyn.ListSymmetryOption RESULT = null;
		 RESULT = new grammar.Absyn.ListSymmetryOption(); 
              CUP$parser$result = new java_cup.runtime.Symbol(13/*ListSymmetryOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 26: // SymmetryOption ::= _SYMB_23 _SYMB_0 ListName _SYMB_1 
            {
              grammar.Absyn.SymmetryOption RESULT = null;
		grammar.Absyn.ListName p_3 = (grammar.Absyn.ListName)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = new grammar.Absyn.RotationWithSymmetry(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(12/*SymmetryOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 25: // SymmetryOption ::= _SYMB_22 
            {
              grammar.Absyn.SymmetryOption RESULT = null;
		 RESULT = new grammar.Absyn.RotationSymmetry(); 
              CUP$parser$result = new java_cup.runtime.Symbol(12/*SymmetryOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 24: // ListVerifierOption ::= ListVerifierOption VerifierOption _SYMB_3 
            {
              grammar.Absyn.ListVerifierOption RESULT = null;
		grammar.Absyn.ListVerifierOption p_1 = (grammar.Absyn.ListVerifierOption)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.VerifierOption p_2 = (grammar.Absyn.VerifierOption)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = p_1; p_1.addLast(p_2); 
              CUP$parser$result = new java_cup.runtime.Symbol(11/*ListVerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 23: // ListVerifierOption ::= 
            {
              grammar.Absyn.ListVerifierOption RESULT = null;
		 RESULT = new grammar.Absyn.ListVerifierOption(); 
              CUP$parser$result = new java_cup.runtime.Symbol(11/*ListVerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 22: // VerifierOption ::= _SYMB_21 _SYMB_2 MyInteger 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		String p_3 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.ParLevel(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 21: // VerifierOption ::= _SYMB_17 _SYMB_2 MyInteger 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		String p_3 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.LogLevel(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 20: // VerifierOption ::= _SYMB_20 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		 RESULT = new grammar.Absyn.NoPrecomputedInv(); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 19: // VerifierOption ::= _SYMB_19 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		 RESULT = new grammar.Absyn.MonolithicWitness(); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 18: // VerifierOption ::= _SYMB_26 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		 RESULT = new grammar.Absyn.UseRankingFunctions(); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 17: // VerifierOption ::= _SYMB_14 _SYMB_2 MyInteger 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		String p_3 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.ExplicitChecks(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 16: // VerifierOption ::= _SYMB_24 _SYMB_2 ListSymmetryOption 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		grammar.Absyn.ListSymmetryOption p_3 = (grammar.Absyn.ListSymmetryOption)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.SymmetryOptions(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 15: // VerifierOption ::= _SYMB_16 _SYMB_2 MyInteger _SYMB_6 MyInteger 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		String p_3 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		String p_5 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.NumOfInitStatesAutomatonGuess(p_3,p_5); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 14: // VerifierOption ::= _SYMB_12 _SYMB_2 MyInteger _SYMB_6 MyInteger 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		String p_3 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		String p_5 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.NumOfStatesAutomatonGuess(p_3,p_5); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 13: // VerifierOption ::= _SYMB_25 _SYMB_2 MyInteger _SYMB_6 MyInteger 
            {
              grammar.Absyn.VerifierOption RESULT = null;
		String p_3 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		String p_5 = (String)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.NumOfStatesTransducerGuess(p_3,p_5); 
              CUP$parser$result = new java_cup.runtime.Symbol(10/*VerifierOption*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 12: // AutomataAcceptingsRule ::= _SYMB_11 _SYMB_2 ListName _SYMB_3 
            {
              grammar.Absyn.AutomataAcceptingsRule RESULT = null;
		grammar.Absyn.ListName p_3 = (grammar.Absyn.ListName)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = new grammar.Absyn.AutomataAcceptings(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(9/*AutomataAcceptingsRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 11: // AutomataTransitionRule ::= Name _SYMB_4 Name 
            {
              grammar.Absyn.AutomataTransitionRule RESULT = null;
		grammar.Absyn.Name p_1 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.Name p_3 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.AutomataEmptyTransition(p_1,p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(8/*AutomataTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 10: // AutomataTransitionRule ::= Name _SYMB_4 Name Name 
            {
              grammar.Absyn.AutomataTransitionRule RESULT = null;
		grammar.Absyn.Name p_1 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-3)).value;
		grammar.Absyn.Name p_3 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		grammar.Absyn.Name p_4 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.AutomataTransition(p_1,p_3,p_4); 
              CUP$parser$result = new java_cup.runtime.Symbol(8/*AutomataTransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 9: // AutomataInitRule ::= _SYMB_15 _SYMB_2 Name _SYMB_3 
            {
              grammar.Absyn.AutomataInitRule RESULT = null;
		grammar.Absyn.Name p_3 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = new grammar.Absyn.AutomataInitialState(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(7/*AutomataInitRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 8: // AutomatonRule ::= _SYMB_0 AutomataInitRule ListAutomataTransitionRule AutomataAcceptingsRule _SYMB_1 
            {
              grammar.Absyn.AutomatonRule RESULT = null;
		grammar.Absyn.AutomataInitRule p_2 = (grammar.Absyn.AutomataInitRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-3)).value;
		grammar.Absyn.ListAutomataTransitionRule p_3 = (grammar.Absyn.ListAutomataTransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.AutomataAcceptingsRule p_4 = (grammar.Absyn.AutomataAcceptingsRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = new grammar.Absyn.Automaton(p_2,p_3,p_4); 
              CUP$parser$result = new java_cup.runtime.Symbol(6/*AutomatonRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 7: // AcceptingRule ::= _SYMB_11 _SYMB_2 ListName _SYMB_3 
            {
              grammar.Absyn.AcceptingRule RESULT = null;
		grammar.Absyn.ListName p_3 = (grammar.Absyn.ListName)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = new grammar.Absyn.TransducerAccepting(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(5/*AcceptingRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 6: // TransitionRule ::= _SYMB_18 _SYMB_2 ListName 
            {
              grammar.Absyn.TransitionRule RESULT = null;
		grammar.Absyn.ListName p_3 = (grammar.Absyn.ListName)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.LoopingTransition(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(4/*TransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 5: // TransitionRule ::= Name _SYMB_4 Name 
            {
              grammar.Absyn.TransitionRule RESULT = null;
		grammar.Absyn.Name p_1 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.Name p_3 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.EmptyTransition(p_1,p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(4/*TransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 4: // TransitionRule ::= Name _SYMB_4 Name Name _SYMB_5 Name 
            {
              grammar.Absyn.TransitionRule RESULT = null;
		grammar.Absyn.Name p_1 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-5)).value;
		grammar.Absyn.Name p_3 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-3)).value;
		grammar.Absyn.Name p_4 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.Name p_6 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.FulTransition(p_1,p_3,p_4,p_6); 
              CUP$parser$result = new java_cup.runtime.Symbol(4/*TransitionRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 3: // InitRule ::= _SYMB_15 _SYMB_2 Name _SYMB_3 
            {
              grammar.Absyn.InitRule RESULT = null;
		grammar.Absyn.Name p_3 = (grammar.Absyn.Name)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = new grammar.Absyn.TransducerInitialState(p_3); 
              CUP$parser$result = new java_cup.runtime.Symbol(3/*InitRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 2: // TransducerRule ::= _SYMB_0 InitRule ListTransitionRule AcceptingRule _SYMB_1 
            {
              grammar.Absyn.TransducerRule RESULT = null;
		grammar.Absyn.InitRule p_2 = (grammar.Absyn.InitRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-3)).value;
		grammar.Absyn.ListTransitionRule p_3 = (grammar.Absyn.ListTransitionRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-2)).value;
		grammar.Absyn.AcceptingRule p_4 = (grammar.Absyn.AcceptingRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		 RESULT = new grammar.Absyn.Transducer(p_2,p_3,p_4); 
              CUP$parser$result = new java_cup.runtime.Symbol(2/*TransducerRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 1: // ModelRule ::= _SYMB_9 AutomatonRule MaybeClosed _SYMB_10 TransducerRule _SYMB_8 AutomatonRule ListVerifierOption 
            {
              grammar.Absyn.ModelRule RESULT = null;
		grammar.Absyn.AutomatonRule p_2 = (grammar.Absyn.AutomatonRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-6)).value;
		grammar.Absyn.MaybeClosed p_3 = (grammar.Absyn.MaybeClosed)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-5)).value;
		grammar.Absyn.TransducerRule p_5 = (grammar.Absyn.TransducerRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-3)).value;
		grammar.Absyn.AutomatonRule p_7 = (grammar.Absyn.AutomatonRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		grammar.Absyn.ListVerifierOption p_8 = (grammar.Absyn.ListVerifierOption)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-0)).value;
		 RESULT = new grammar.Absyn.Model(p_2,p_3,p_5,p_7,p_8); 
              CUP$parser$result = new java_cup.runtime.Symbol(1/*ModelRule*/, RESULT);
            }
          return CUP$parser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 0: // $START ::= ModelRule EOF 
            {
              Object RESULT = null;
		grammar.Absyn.ModelRule start_val = (grammar.Absyn.ModelRule)((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top-1)).value;
		RESULT = start_val;
              CUP$parser$result = new java_cup.runtime.Symbol(0/*$START*/, RESULT);
            }
          /* ACCEPT */
          CUP$parser$parser.done_parsing();
          return CUP$parser$result;

          /* . . . . . .*/
          default:
            throw new Exception(
               "Invalid action number found in internal parse table");

        }
    }
}

