package javax.mail.search;

public abstract class IntegerComparisonTerm extends ComparisonTerm
{
    protected int number;
    private static final long serialVersionUID = -6963571240154302484L;
    
    protected IntegerComparisonTerm(final int comparison, final int number) {
        this.comparison = comparison;
        this.number = number;
    }
    
    public int getNumber() {
        return this.number;
    }
    
    public int getComparison() {
        return this.comparison;
    }
    
    protected boolean match(final int i) {
        switch (this.comparison) {
            case 1: {
                return i <= this.number;
            }
            case 2: {
                return i < this.number;
            }
            case 3: {
                return i == this.number;
            }
            case 4: {
                return i != this.number;
            }
            case 5: {
                return i > this.number;
            }
            case 6: {
                return i >= this.number;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof IntegerComparisonTerm)) {
            return false;
        }
        final IntegerComparisonTerm ict = (IntegerComparisonTerm)obj;
        return ict.number == this.number && super.equals(obj);
    }
    
    public int hashCode() {
        return this.number + super.hashCode();
    }
}
