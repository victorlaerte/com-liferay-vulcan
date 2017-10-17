/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.vulcan.sample.liferay.portal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import com.liferay.portal.kernel.model.User;
import com.liferay.vulcan.sample.liferay.portal.internal.resource.BlogPostingScopedCollectionResource;

import java.util.function.Function;

import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Javier Gamarra
 */
public class BlogPostingScopedCollectionCollectionResourceTest
	extends CollectionResourceTest {

	@Test
	public void testBuildRepresentor() {
		BlogPostingScopedCollectionResource
			blogPostingScopedCollectionResource =
				new BlogPostingScopedCollectionResource();

		blogPostingScopedCollectionResource.buildRepresentor(
			representorBuilderSpy);

		Mockito.verify(
			representorBuilderSpy
		).identifier(
			any(Function.class)
		);

		verifyIdentifier().addBidirectionalModel(
			eq("webSite"), eq("blogs"), any(Class.class), any(Function.class),
			any(Function.class));

		verifyIdentifier().addDate(eq("createDate"), any(Function.class));
		verifyIdentifier().addDate(eq("displayDate"), any(Function.class));
		verifyIdentifier().addDate(eq("modifiedDate"), any(Function.class));
		verifyIdentifier().addDate(eq("publishedDate"), any(Function.class));
		verifyIdentifier().addEmbeddedModel(
			eq("aggregateRating"), any(Class.class), any(Function.class));
		verifyIdentifier().addEmbeddedModel(
			eq("creator"), eq(User.class), any(Function.class));
		verifyIdentifier().addLink(
			eq("license"), eq("https://creativecommons.org/licenses/by/4.0"));
		verifyIdentifier().addLinkedModel(
			eq("author"), eq(User.class), any(Function.class));
		verifyIdentifier().addRelatedCollection(
			eq("comment"), any(Class.class), any(Function.class));
		verifyIdentifier().addString(eq("alternativeHeadline"), any(Function.class));
		verifyIdentifier().addString(eq("articleBody"), any(Function.class));
		verifyIdentifier().addString(eq("description"), any(Function.class));
		verifyIdentifier().addString(eq("fileFormat"), any(Function.class));
		verifyIdentifier().addString(eq("headline"), any(Function.class));
		verifyIdentifier().addType(eq("BlogPosting"));
	}

}