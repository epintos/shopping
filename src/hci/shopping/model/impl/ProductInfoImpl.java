package hci.shopping.model.impl;

import hci.shopping.model.api.ProductInfo;
import android.graphics.drawable.Drawable;

public class ProductInfoImpl extends ProductImpl implements ProductInfo {

	private String actors;
	private String format;
	private String subtitles;
	private String region;
	private String aspectRatio;
	private String numberDiscs;
	private String releaseDate;
	private String runTime;
	private String ASIN;
	

	private String authors;
	private String publisher;
	private String published_date;
	private String isbn_10;
	private String isbn_13;
	private String language;

	public ProductInfoImpl(String ID, String name, Drawable image,
			String ranking, String price) {
		super(ID, name, image, ranking, price);
	}
	
	public ProductInfoImpl(String ID, String name, Drawable image,
			String ranking, String price, String authors, String publisher,
			String published_date, String isbn_10,String language) {
		super(ID, name, image, ranking, price);
		this.authors = authors;
		this.publisher = publisher;
		this.published_date = published_date;
		this.isbn_10 = isbn_10;
		this.language = language;
	}

	public ProductInfoImpl(String ID, String name, Drawable image,
			String ranking, String price, String actors, String format,
			String subtitles, String runTime) {
		super(ID, name, image, ranking, price);
		this.actors = actors;
		this.format = format;
		this.subtitles = subtitles;
		this.runTime = runTime;
	}

	public String getActors() {
		return actors;
	}

	public String getFormat() {
		return format;
	}

	public String getLanguage() {
		return language;
	}

	public String getSubtitles() {
		return subtitles;
	}

	public String getRegion() {
		return region;
	}

	public String getAspectRatio() {
		return aspectRatio;
	}

	public String getNumberDiscs() {
		return numberDiscs;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public String getRunTime() {
		return runTime;
	}

	public String getASIN() {
		return ASIN;
	}
	public String getAuthors() {
		return authors;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getPublishedDate() {
		return published_date;
	}

	public String getISBN10() {
		return isbn_10;
	}

	public String getISBN13() {
		return isbn_13;
	}

}
