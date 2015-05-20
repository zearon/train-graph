package org.paradise.etrc.util.data;

import com.sun.accessibility.internal.resources.accessibility;

public class Tuple3 <T1, T2, T3> {
	
	public T1 A;
	public T2 B;
	public T3 C;
	
	protected boolean fullEqual = false;
	protected boolean reverseEqual = false;
	
	public Tuple3(T1 A, T2 B, T3 C) {
		this.A = A;
		this.B = B;
		this.C = C;
	}
	
	public Tuple3(T1 A, T2 B, T3 C, boolean fullEqual) {
		this(A, B, C);
		this.fullEqual = fullEqual;
	}
	
	/**
	 * Get instance. Apply equals on only A component.
	 * @param A
	 * @param B
	 * @param C
	 * @return
	 */
	public static <T1, T2, T3> Tuple3<T1, T2, T3>  ofKey (T1 A, T2 B, T3 C) {
		return of (A, B, C, false);
	}
	
	/**
	 * Get instance. Apply equals on Both components.
	 * @param A
	 * @param B
	 * @return
	 */
	public static <T1, T2, T3> Tuple3<T1, T2, T3>  of (T1 A, T2 B, T3 C) {
		return of (A, B, C, true);
	}
	
	public static <T1, T2, T3> Tuple3<T1, T2, T3>  of (T1 A, T2 B, T3 C, 
			boolean fullEqual) {
		return new Tuple3<T1, T2, T3> (A, B, C, fullEqual);
	}

	@Override
	public int hashCode() {
		int a = A == null ? 1 : A.hashCode();
		int b = B == null ? 1 : B.hashCode();
		int c = C == null ? 1 : C.hashCode();
		if (fullEqual)
			return a << 2 + (b << 1 + c);
		else 
			return a;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (obj instanceof Tuple3) {
			Tuple3 t2 = (Tuple3) obj;

			boolean aEqual = (A != null && A.equals(t2.A)) || (A == null && t2.A == null);
			if (fullEqual || t2.fullEqual) {
				boolean bEqual = B != null && B.equals(t2.B) || (B == null && t2.B == null);
				boolean cEqual = C != null && C.equals(t2.C) || (C == null && t2.C == null);
				return aEqual && bEqual && cEqual;
			} else {
				return aEqual;
			}
		} else {
			return false;
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Tuple3<T1, T2, T3> tuple2 =  new Tuple3<T1, T2, T3>(A, B, C);
		tuple2.fullEqual = fullEqual;
		return tuple2;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", A, B, C);
	}

}