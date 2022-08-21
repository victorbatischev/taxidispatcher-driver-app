package com.psdevelop.tdandrapp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
    	OrderTarif orderTarif = new OrderTarif(1, "city", 1, 1);

		//orderTarif.areaLines.add(new TarifAreaLine(57.8978, 34.0736)); //ранее 4 точка по правилам
		//orderTarif.areaLines.add(new TarifAreaLine(57.8767, 34.1139)); //ранее 5 точка по правилам
		//orderTarif.areaLines.add(new TarifAreaLine(57.8611, 34.0739)); //ранее 1 точка по правилам
		//orderTarif.areaLines.add(new TarifAreaLine(57.875, 34.03)); //ранее 2 точка по правилам
		//orderTarif.areaLines.add(new TarifAreaLine(57.8946, 34.0327)); //ранее 3 точка по правилам

		orderTarif.areaLines.add(new TarifAreaLine(34.1149, 34.0736)); //ранее 4 точка по правилам
		orderTarif.areaLines.add(new TarifAreaLine(57.8767, 34.1139)); //ранее 5 точка по правилам
		orderTarif.areaLines.add(new TarifAreaLine(57.8611, 34.0739)); //ранее 1 точка по правилам
		orderTarif.areaLines.add(new TarifAreaLine(57.875, 34.03)); //ранее 2 точка по правилам
		orderTarif.areaLines.add(new TarifAreaLine(57.8946, 34.0327)); //ранее 3 точка по правилам

		//System.out.println("Test inPoly in: " + orderTarif.inPoly(34.098905,44.936591 ));
		//System.out.println("Test inPoly out: " + orderTarif.inPoly(34.098905,44.936591 ));

		System.out.println("Test isPointInsidePolygon in: " +
				orderTarif.isPointInsidePolygon(34.098905,44.936591, true ));
		//System.out.println("Test isPointInsidePolygon in: " +
				//orderTarif.isPointInsidePolygon(34.053776,57.885626, true ));
		//System.out.println("Test isPointInsidePolygon out: " +
				//orderTarif.isPointInsidePolygon(38.053776,57.885626, true ));
		//System.out.println("Test isPointInsidePolygon out near top Top: " +
				//orderTarif.isPointInsidePolygon(34.072461,57.899098, true ));
		//57.896356, 34.072982
		//System.out.println("Test isPointInsidePolygon in near bot Top: " +
				//orderTarif.isPointInsidePolygon(34.072982,57.896356, true ));

		//System.out.println("Test isPointInsidePolygon in near Right Top side center: " +
				//orderTarif.isPointInsidePolygon(34.090105,57.887192, true ));
		//57.887964, 34.095205
		//System.out.println("Test isPointInsidePolygon out near Right Top side center: " +
				//orderTarif.isPointInsidePolygon(34.095205,57.887964, true ));

		//точка сбоя
		//System.out.println("Test isPointInsidePolygon in near Right Top side center: " +
				//orderTarif.isPointInsidePolygon(34.079,57.877, true ));

		assertEquals(4, 2 + 2);
    }
}
