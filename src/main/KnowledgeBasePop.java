package main;

public class KnowledgeBasePop {

	float[][][] fuzzyParams;
	float fitness;
	MersenneTwisterFast uniqueRnd;

	public KnowledgeBasePop(SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast( setting.rnd.nextInt() );
	}

	public KnowledgeBasePop(KnowledgeBasePop oldKB) {
		int Ndim = oldKB.fuzzyParams.length;
		int Fdiv = oldKB.fuzzyParams[0].length;
		this.fuzzyParams = new float[Ndim][Fdiv][2];
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				this.fuzzyParams[dim_i][div_i][0] = oldKB.fuzzyParams[dim_i][div_i][0];
				this.fuzzyParams[dim_i][div_i][1] = oldKB.fuzzyParams[dim_i][div_i][1];
			}
		}
		this.fitness = oldKB.fitness;
		this.uniqueRnd = new MersenneTwisterFast(oldKB.uniqueRnd.nextInt());
	}

	public void setFuzzyParams(float[][][] _fuzzyParams) {
		int Ndim = _fuzzyParams.length;
		int Fdiv = _fuzzyParams[0].length;
		this.fuzzyParams = new float[Ndim][Fdiv][2];

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				this.fuzzyParams[dim_i][div_i][0] = _fuzzyParams[dim_i][div_i][0];
				this.fuzzyParams[dim_i][div_i][1] = _fuzzyParams[dim_i][div_i][1];
			}
		}
	}

	public float[][][] getFuzzyParams() {
		return this.fuzzyParams;
	}

	public void setFitness(float _fitness) {
		this.fitness = _fitness;
	}

	public float getFitness() {
		return this.fitness;
	}
}
