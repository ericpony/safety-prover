package grammar.Absyn; // Java Package generated by the BNF Converter.

public class ParLevel extends VerifierOption {
  public final String myinteger_;
  public ParLevel(String p1) { myinteger_ = p1; }

  public <R,A> R accept(grammar.Absyn.VerifierOption.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof grammar.Absyn.ParLevel) {
      grammar.Absyn.ParLevel x = (grammar.Absyn.ParLevel)o;
      return this.myinteger_.equals(x.myinteger_);
    }
    return false;
  }

  public int hashCode() {
    return this.myinteger_.hashCode();
  }


}