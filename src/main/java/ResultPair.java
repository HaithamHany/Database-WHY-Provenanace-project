public class ResultPair {
    final String ann1;
    final String ann2;

    public ResultPair(String ann1, String ann2) {
        this.ann1 = ann1;
        this.ann2 = ann2;
    }

    public String toString() {
        return " {"+ann1+","+ann2+"}";
    }
}
