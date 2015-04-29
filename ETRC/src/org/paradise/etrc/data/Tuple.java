package org.paradise.etrc.data;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class Tuple <T1, T2> {
	
	public T1 A;
	public T2 B;
	
	protected boolean fullEqual = false;
	
	public Tuple(T1 A, T2 B) {
		this.A = A;
		this.B = B;
	}
	public Tuple(T1 A, T2 B, boolean fullEqual) {
		this.A = A;
		this.B = B;
		this.fullEqual = fullEqual;
	}
	
	/**
	 * Get instance. Apply equals on only A component.
	 * @param A
	 * @param B
	 * @return
	 */
	public static <T1, T2> Tuple<T1, T2>  of (T1 A, T2 B) {
		return of (A, B, false);
	}
	
	/**
	 * Get instance. Apply equals on Both components.
	 * @param A
	 * @param B
	 * @return
	 */
	public static <T1, T2> Tuple<T1, T2>  oF (T1 A, T2 B) {
		return of (A, B, true);
	}
	
	public static <T1, T2> Tuple<T1, T2>  of (T1 A, T2 B, boolean fullEqual) {
		return new Tuple<T1, T2> (A, B, fullEqual);
	}

	@Override
	public int hashCode() {
		if (fullEqual)
			return A.hashCode() * B.hashCode();
		else
			return A.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tuple) {
			Tuple t2 = (Tuple) obj;
			boolean aEqual = (A != null && A.equals(t2.A)) || (A == null && t2.A == null);
			if (fullEqual) {
				boolean bEqual = B != null && B.equals(t2.B) || (B == null && t2.B == null);
				return aEqual && bEqual;
			} else {
				return aEqual;
			}
		} else {
			return false;
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Tuple<T1, T2>(A, B);
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", A, B);
	}

}
