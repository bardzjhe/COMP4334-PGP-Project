package trustmodel;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 23/11/2023
 * @Description:
 */
public enum TrustLevel{
    FULL(1.0),
    PARTIAL(0.5),
    NONE(0.0);

    private final double value;

    TrustLevel(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

}
