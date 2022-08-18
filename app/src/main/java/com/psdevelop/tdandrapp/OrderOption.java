package com.psdevelop.tdandrapp;

public class OrderOption {
	
	int id=0;
	String option_name="";
	double opt_coeff=1;
	double opt_composed=0;
	int tplan_id=-1;
	String short_name="";

	public OrderOption(int oid, String name, double coeff, double composed) {
		// TODO Auto-generated constructor stub
		this.id = oid;
		this.option_name = name;
		this.opt_coeff = coeff;
		this.opt_composed = composed;
	}

}
