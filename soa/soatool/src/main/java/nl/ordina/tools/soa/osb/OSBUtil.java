package nl.ordina.tools.soa.osb;

public class OSBUtil {
	/**
	 * Do nothing for milliseconds time
	 * @param milliseconds
	 */
	public static void idle(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {

		}

	}
	/**
     * Geef een boolean terug in percentage van aantal gevallen.
     * 
      * B.v. percentage = 25 => in 25 van de 100 gevallen wordt true terug
     * gegeven
     * 
      * @param percentage
     */
     public static boolean randomTrue(int percentage) {
           boolean result = false;

           if (Math.random() * 100 <= percentage) {
                  result = true;
           }

           return result;
     }
}
