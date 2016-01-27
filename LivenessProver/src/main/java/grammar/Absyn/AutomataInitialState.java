package grammar.Absyn; // Java Package generated by the BNF Converter.

public class AutomataInitialState extends AutomataInitRule {
  public final Name name_;

  public AutomataInitialState(Name p1) { name_ = p1; }

  public <R,A> R accept(grammar.Absyn.AutomataInitRule.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof grammar.Absyn.AutomataInitialState) {
      grammar.Absyn.AutomataInitialState x = (grammar.Absyn.AutomataInitialState)o;
      return this.name_.equals(x.name_);
    }
    return false;
  }

  public int hashCode() {
    return this.name_.hashCode();
  }


}
