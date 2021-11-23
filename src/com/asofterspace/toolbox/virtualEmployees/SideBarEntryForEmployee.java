/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.virtualEmployees;


public class SideBarEntryForEmployee implements SideBarEntry {

	private String employee;


	public SideBarEntryForEmployee(String employee) {
		if (employee != null) {
			this.employee = employee.toLowerCase();
		}
	}

	public String getName() {
		return employee;
	}

	@Override
	public boolean equals(Object other) {

		// If the other one does not even exist, we are not the same - because we exist!
		if (other == null) {
			return false;
		}

		if (other instanceof SideBarEntryForEmployee) {
			SideBarEntryForEmployee otherSideBarEntryForEmployee = (SideBarEntryForEmployee) other;

			// If our values for employee are different...
			if (this.employee == null) {
				if (otherSideBarEntryForEmployee.employee != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.employee.equals(otherSideBarEntryForEmployee.employee)) {
				// ... then we are not the same!
				return false;
			}

			// We have no reason to assume that we are not the same
			return true;
		}

		// If the other one cannot even be cast to us, then we are not the same!
		return false;
	}

	@Override
	public int hashCode() {
		return this.employee.hashCode();
	}

}
