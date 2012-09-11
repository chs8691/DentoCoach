package de.cs.android.putzi;

/**
 * Represent the speed and supports a factory method.
 * 
 * @author ChristianSchulzendor
 * 
 */
public enum Speed {
	SLOW(3), MEDIUM(2), FAST(1);

	/**
	 * Converts every value to a Speed
	 * 
	 * @param value
	 *            Int in range [1..3]. Other values will be set to the
	 *            corresponding border (1 or 3)
	 * 
	 * @return Speed
	 */
	public static Speed create(int value) {
		if (value < 1)
			value = 1;
		else if (value > 3)
			value = 3;
		switch (value) {
		case 2:
			return Speed.MEDIUM;
		case 1:
			return Speed.FAST;
		default:
			return Speed.SLOW;
		}
	}

	private final int value;

	private Speed(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}