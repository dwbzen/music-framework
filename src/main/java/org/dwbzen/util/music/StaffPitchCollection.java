package org.dwbzen.util.music;

import java.util.HashMap;
import java.util.Map;

import org.dwbzen.common.util.IJson;

/**
 * StaffPitchCollection aggregates Pitch/PitchSet for a Staff. This could have 1, 2 or 3 individual staffs<br>
 * depending on the Instrument. For example Piano and Harpischord require 2 staffs, a PipeOrgan needs 3.
 * 
 * @author don_bacon
 *
 */
public class StaffPitchCollection implements IJson, Cloneable {

	private Map<Integer, PitchCollection> staffPitches = new HashMap<>();
	
	public StaffPitchCollection() {
		staffPitches.put(1, new PitchCollection());
	}
	public StaffPitchCollection(int numberOfStaves) {
		for(int i=1; i<=numberOfStaves; i++) {
			staffPitches.put(i, new PitchCollection());
		}
	}
	
	public Map<Integer, PitchCollection> getStaffPitches() {
		return staffPitches;
	}
	
	public PitchCollection getPitchCollection(int staffNumber) {
		PitchCollection pc = null;
		if(staffPitches.containsKey(staffNumber)) {
			pc = staffPitches.get(staffNumber);
		}
		else {
			pc = new PitchCollection();
			staffPitches.put(staffNumber, pc);
		}
		return pc;
	}
	
	public int getNumberOfStaves() {
		return staffPitches.keySet().size();
	}
	
	public boolean hasStaff(int staffNumber) {
		return staffPitches.keySet().contains(staffNumber);
	}
	
	public void addPitchCollection(PitchCollection pc, int staffNumber) {
		PitchCollection pitchCollection = getPitchCollection(staffNumber);
		pitchCollection.add(pc);
	}
	
	public StaffPitchCollection getRetrograde() {
		StaffPitchCollection spc = new StaffPitchCollection(getNumberOfStaves());
		for(Integer staffNumber : staffPitches.keySet()) {
			PitchCollection pc = staffPitches.get(staffNumber);
			spc.addPitchCollection(pc.getRetrograde(), staffNumber);
		}
		return spc;
	}
}
