package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UtilizationModelPlanetLabInMemoryMax implements UtilizationModel {
	
	/** The scheduling interval. */
	private double schedulingInterval;

	/** The data (5 min * 288 = 24 hours). */
	private final double[] data;
	
	/**
	 * Instantiates a new utilization model PlanetLab.
	 * 
	 * @param inputPath the input path
	 * @throws NumberFormatException the number format exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public UtilizationModelPlanetLabInMemoryMax(String inputPath, double schedulingInterval)
			throws NumberFormatException,
			IOException {
		data = new double[289]; 
//		data = new double[5];
		setSchedulingInterval(schedulingInterval);
		BufferedReader input = new BufferedReader(new FileReader(inputPath));
		//System.out.println(inputPath);
		int n = data.length;
		
		
		/*for (int i = 0; i < n - 1; i++) {
			data[i] = Integer.valueOf(input.readLine()) / 100.0;
		//	System.out.println("a " + data[i]);
		}*/
		
		for (int i = 0; i < n - 1; i++) {
			data[i] = Double.valueOf(input.readLine()); // values in each files - put into array by line
//			System.out.println("a " + i);
		}
		data[n - 1] = data[n - 2];
		input.close();
	}
	

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		if (time % getSchedulingInterval() == 0) {
			 //System.out.println("DATA " + time + " " + data[(int) time / (int) getSchedulingInterval()]);
			return data[(int) time / (int) getSchedulingInterval()];
		}
		int time1 = (int) Math.floor(time / getSchedulingInterval());
		int time2 = (int) Math.ceil(time / getSchedulingInterval());
		double utilization1 = data[time1];
		double utilization2 = data[time2];
		double delta = (utilization2 - utilization1) / ((time2 - time1) * getSchedulingInterval());
		double utilization = utilization1 + delta * (time - time1 * getSchedulingInterval());
		
//		System.out.println(time + " " + time1 + " " + time2 + " " + utilization1 + " " + utilization2 + " " + delta + " " + utilization);
		return utilization;

	}
	
	public void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

	/**
	 * Gets the scheduling interval.
	 * 
	 * @return the scheduling interval
	 */
	public double getSchedulingInterval() {
		return schedulingInterval;
	}
}