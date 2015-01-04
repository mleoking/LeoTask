package org.leores.demo;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.leores.util.U;

public class ObjUtilDemo extends Demo {
	public Book[] books;

	public static class Book {
		private Integer id;
		protected Integer id2;
		public String name;
		public String name2;
		public Integer[] catgorigies;
		public Double fullPrice;
		public Double discount;
		public List<Double> discountHistory;
		protected static Integer nBook = 0;
		protected Book book;

		public Book() {
		}

		public Book(int id2, String name, double fullPrice, Double discount) {
			id = nBook;
			this.id2 = id2;
			this.name = name;
			this.fullPrice = fullPrice;
			this.discount = discount;
			nBook++;
		}

		public double getPrice() {
			return fullPrice * discount;
		}

		public void setPrice(Double price) {//here it has to be ``Double''. ``double'' does not work.
			discount = price / fullPrice;
			return;
		}

		public void setPrice(String sPrice) {
			setPrice(new Double(sPrice));
		}

		public String info() {
			return U.toStr(this) + U.toStr(book);
		}

		public String method1() {
			return "method1 called";
		}

		public String method2(String str1, String str2, String str3) {
			return "method2 called with " + str1 + "+" + str2 + "+" + str3;
		}
	}

	public ObjUtilDemo() {
		books = new Book[3];
		books[0] = new Book(100, "Book0", 100, 0.5);
		books[1] = new Book(101, "Book1", 200, 0.5);
		books[2] = new Book(102, "Book2", 300, 0.5);
	}

	public void getSetFieldValue() {
		log("Book0: " + U.toStr(books[0]));
		//get private and protected field value.
		log("id=" + U.getFieldValue(books[0], "id", U.modAll));
		log("id2=" + U.getFieldValue(books[0], "id2", U.modAll));
		double price = (Double) U.getFieldValue(books[0], "price");//getFieldValue calls getPrice() if exists.
		log("price: " + price);
		U.setFieldValue(books[0], "price", 30.0);//setFieldValue calls setPrice(30.0) if it exists.
		log("After set price \nBook0: " + U.toStr(books[0]));
		U.setFieldValue(books[0], "fullPrice", "50", null, true, true);//Use a string to set value;
		log("After set price2 \nBook0: " + U.toStr(books[0]));
		price = (Double) U.getFieldValue(books[0], "price");
		log("price: " + price);
		log("");
		log("Set Field value through evaluation:");
		log("Before setting:", U.toStr(books[1]));
		U.setFieldValue(books[1], "id2", "#$fullPrice$+$id$#", U.modAll, true, true);
		U.setFieldValue(books[1], "name", "$name$(id2:$id2$ $price$/(3*$fullPrice$)=#ROUND($price$*100/(3*$fullPrice$),2)#%)");
		log("After setting:", U.toStr(books[1]));
	}

	public void loadObjFromString() {
		Book book = new Book();
		String str = "id=111;id2=222;name=Hello world;fullPrice=20;discount=0.5;";
		boolean loaded = U.loadFromString(book, str);
		log("loaded:" + loaded + " " + U.toStr(book, U.modAll));
	}

	public void evaluate() {
		String str1 = "#A evualtion section should be in one line! $\n $id$+$fullPrice$*$discount$=#$id$+$fullPrice$*$discount$# #  #";
		String str2 = "obj:$%$\nname:$name$\n id2:$id2$\n price:$price$ $$";
		String str3 = "str1->" + str1 + " str2->" + str2 + " invalid-exp=#invalid-exp# unknown:$unknown$";

		books[1].name = null;
		log(U.eval(str1, books[1]));
		log(U.eval(str2, books[1]));
		log("str3=" + str3);
		try {
			log(U.eval(str3, books[1]));
		} catch (RuntimeException e) {
			log(e);
		}

		log("Ignoring invalid evaluation sections (no exceptions (but could have null) in result):");
		log(U.eval(str3, books[1], U.EVAL_InvalidIgnore));
		log("------------------------------------");
		log("Also ignore null:");
		log(U.eval(str3, books[1], U.EVAL_InvalidIgnore | U.EVAL_NullIgnore));

		String str4 = "#$fullPrice$>200# #$fullPrice$=200# #$fullPrice$<200# #$fullPrice$<200||$fullPrice$>200# #$fullPrice$>199&&$fullPrice$<201#";
		log(str4 + ":" + U.eval(str4, books[1]));

		String str5 = "150>140";
		BigDecimal bd5 = U.eval1Expression(str5);
		log(str5 + ":" + bd5 + (bd5.intValue() > 0));

		String str6 = "#$%$^2#";
		log(str6 + ":" + U.eval(str6, 2));

		String str7 = "#ROUND(123.456,0)# #ROUND(123.456,1)# #ROUND(123.456,2)#";
		log(str7 + ":" + U.eval(str7, null));
	}

	public void loadFromXML() {
		Book book = new Book();
		U.loadFromXML(book, "book.xml");
		log(book.info());
	}

	public void copy() {
		Book book1 = new Book(1, "book1", 100d, 0.5), book2 = new Book();
		book1.catgorigies = new Integer[] { 1, 2, 3 };
		book1.discountHistory = Arrays.asList(new Double[] { 0.1, 0.2, 0.3 });
		log(book1.discount, U.asList(book1.catgorigies), book1.discountHistory);
		log(book2.discount, U.asList(book2.catgorigies), book2.discountHistory);
		U.copy(book2, book1);
		log("After copy:");
		log(book1.discount, U.asList(book1.catgorigies), book1.discountHistory);
		log(book2.discount, U.asList(book2.catgorigies), book2.discountHistory);
		book2.discount = 0.2;
		book2.catgorigies[1] = 200;
		book2.discountHistory.set(2, 0.2);
		log("After changes:");
		log(book1.discount, U.asList(book1.catgorigies), book1.discountHistory);
		log(book2.discount, U.asList(book2.catgorigies), book2.discountHistory);
	}

	public static void demo() {
		ObjUtilDemo demo = new ObjUtilDemo();
		demo.getSetFieldValue();
		demo.loadObjFromString();
		demo.evaluate();
		demo.loadFromXML();
		demo.copy();
	}
}
