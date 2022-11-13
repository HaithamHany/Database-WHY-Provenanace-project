public class Tuple {
    final String ann1;
    final String ann2;
    public final int valueId;

    public Tuple(String ann1, String ann2, int valueId) {
        this.ann1 = ann1;
        this.ann2 = ann2;
        this.valueId = valueId;
    }

    public int getValueId()
    {
        return valueId;
    }
    public String toString() {
        return this.valueId + " {"+ann1+","+ann2+"}";
    }
}