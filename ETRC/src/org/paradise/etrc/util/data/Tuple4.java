package org.paradise.etrc.util.data;

import com.sun.accessibility.internal.resources.accessibility;

public class Tuple4 <T1, T2, T3, T4> {
	
	public T1 A;
	public T2 B;
	public T3 C;
	public T4 D;
	
	protected boolean fullEqual = false;
	protected boolean reverseEqual = false;
	
	public Tuple4(T1 A, T2 B, T3 C, T4 D) {
		this.A = A;
		this.B = B;
		this.C = C;
	}
	
	public Tuple4(T1 A, T2 B, T3 C, T4 D, boolean fullEqual) {
		this(A, B, C, D);
		this.fullEqual = fullEqual;
	}
	
	/**
	 * Get instance. Apply equals on only A component.
	 * @param A
	 * @param B
	 * @param C
	 * @return
	 */
	public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>  of (T1 A, T2 B, T3 C, T4 D) {
		return of (A, B, C, D, false);
	}
	
	/**
	 * Get instance. Apply equals on Both components.
	 * @param A
	 * @param B
	 * @return
	 */
	public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>  oF (T1 A, T2 B, T3 C, T4 D) {
		return of (A, B, C, D, true);
	}
	
	public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>  of (T1 A, T2 B, T3 C, T4 D, 
			boolean fullEqual) {
		return new Tuple4<T1, T2, T3, T4> (A, B, C, D, fullEqual);
	}

	@Override
	public int hashCode() {
		int a = A == null ? 1 : A.hashCode();
		int b = B == null ? 1 : B.hashCode();
		int c = C == null ? 1 : C.hashCode();
		int d = D == null ? 1 : D.hashCode();
		if (fullEqual)
			return a * b * c * d;
		else
			return a;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (obj instanceof Tuple4) {
			Tuple4 t2 = (Tuple4) obj;

			boolean aEqual = (A != null && A.equals(t2.A)) || (A == null && t2.A == null);
			if (fullEqual) {
				boolean bEqual = B != null && B.equals(t2.B) || (B == null && t2.B == null);
				boolean cEqual = C != null && C.equals(t2.C) || (C == null && t2.C == null);
				boolean dEqual = D != null && D.equals(t2.D) || (D == null && t2.D == null);
				return aEqual && bEqual && cEqual && dEqual;
			} else {
				return aEqual;
			}
		} else {
			return false;
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Tuple4<T1, T2, T3, T4> tuple2 =  new Tuple4<T1, T2, T3, T4>(A, B, C, D);
		tuple2.fullEqual = fullEqual;
		return tuple2;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s, %s)", A, B, C, D);
	}

}