package util.cp;

public class OccurrenceProbability {
		
		private int occurrence = 0;
		private double probability = 0.0;
		private int[] range = {0, 0};
		
		public OccurrenceProbability() {
		}
		
		public OccurrenceProbability(int occ, double prob) {
			this.occurrence = occ;
			this.probability = prob;
		}

		public int getOccurrence() {
			return occurrence;
		}

		public void setOccurrence(int occurrence) {
			this.occurrence = occurrence;
		}

		public double getProbability() {
			return probability;
		}

		public void setProbability(double probability) {
			this.probability = probability;
		}

		public int[] getRange() {
			return range;
		}

		public void setRange(int[] range) {
			this.range = range;
		}
		public void setRange(int index, int val) {
			range[index] = val;
		}
}
