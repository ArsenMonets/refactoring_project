package сom.github.arsenmonets.refactoringproject.refactored.tpftps;

public class TpfTpsHandler {
	private final float ticksPerSecond;
	private float timeStep = 0f;
	private float ticksPerFrame = 0f;
	private float valSidewaysMove = 0f;
	
	public TpfTpsHandler(float ticksPerSecond) {
		this.ticksPerSecond = ticksPerSecond;
	}
	
	public void updateTimeStep(float tpf) {
		this.timeStep = ticksPerSecond * tpf;
	}
	
	public void setSidewaysVaues(float valOfSidewaysMove, float ticksPerFrame) {
		this.ticksPerFrame = ticksPerFrame;
		this.valSidewaysMove = valOfSidewaysMove;
	}
	
	public float getTimeStep() {
		return this.timeStep;
	}
	
	public float getSidewaysMoveVal() {
		return this.valSidewaysMove * ticksPerSecond / 2; 
	}
	
	public float getCameraTiltCoeff() {
		return this.ticksPerFrame * this.valSidewaysMove; 
	}
	
}
