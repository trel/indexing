/** Code generated by EriLex */
package databook.edsl.map;
public class DmapValueEqAxiom {
	public <E,S,T> F<DmapValue<S,E>,DmapValue<T,E>> appl(
		final F<S,T> eq) {
		return ((F<DmapValue<S,E>,DmapValue<T,E>>) eq) ;
	}
	public <t,S,T> F<DmapValue<t,T>,DmapValue<t,T>> appr(
		final F<S,T> eq) {
		return ((F<DmapValue<t,T>,DmapValue<t,T>>) eq) ;
	}
}
