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

package com.liferay.vulcan.architect.sample.liferay.portal.internal.resource;

import com.liferay.blogs.kernel.exception.NoSuchEntryException;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderService;
import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.vulcan.architect.pagination.PageItems;
import com.liferay.vulcan.architect.pagination.Pagination;
import com.liferay.vulcan.architect.resource.CollectionResource;
import com.liferay.vulcan.architect.resource.Representor;
import com.liferay.vulcan.architect.resource.Routes;
import com.liferay.vulcan.architect.resource.builder.RepresentorBuilder;
import com.liferay.vulcan.architect.resource.builder.RoutesBuilder;
import com.liferay.vulcan.architect.resource.identifier.LongIdentifier;
import com.liferay.vulcan.architect.result.Try;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides all the necessary information to expose folder resource through a
 * web API. <p> The resources are mapped from the internal {@link DLFolder}
 * model.
 *
 * @author Javier Gamarra
 * @review
 */
@Component(immediate = true)
public class FolderCollectionResource
	implements CollectionResource<DLFolder, LongIdentifier> {

	@Override
	public Representor<DLFolder, LongIdentifier> buildRepresentor(
		RepresentorBuilder<DLFolder, LongIdentifier> representorBuilder) {

		Function<Date, String> formatFunction = date -> {
			if (date == null) {
				return null;
			}

			DateFormat dateFormat = DateUtil.getISO8601Format();

			return dateFormat.format(date);
		};

		return representorBuilder.identifier(
			dlFolder -> dlFolder::getFolderId
		).addBidirectionalModel(
			"group", "folders", Group.class, this::_getGroupOptional,
			group -> (LongIdentifier)group::getGroupId
		).addString(
			"dateCreated",
			dlFolder -> formatFunction.apply(dlFolder.getCreateDate())
		).addString(
			"dateModified",
			dlFolder -> formatFunction.apply(dlFolder.getCreateDate())
		).addString(
			"datePublished",
			dlFolder -> formatFunction.apply(dlFolder.getCreateDate())
		).addString(
			"name", DLFolder::getName
		).addString(
			"path", this::_getPath
		).addType(
			"Folder"
		).build();
	}

	@Override
	public String getName() {
		return "folders";
	}

	@Override
	public Routes<DLFolder> routes(
		RoutesBuilder<DLFolder, LongIdentifier> routesBuilder) {

		return routesBuilder.addCollectionPageGetter(
			this::_getPageItems, LongIdentifier.class
		).addCollectionPageItemCreator(
			this::_addDLFolder, LongIdentifier.class
		).addCollectionPageItemGetter(
			this::_getDLFolder
		).addCollectionPageItemRemover(
			this::_deleteDLFolder
		).addCollectionPageItemUpdater(
			this::_updateDLFolder
		).build();
	}

	private DLFolder _addDLFolder(
		LongIdentifier groupIdLongIdentifier, Map<String, Object> body) {

		long parentFolderId = 0;

		String name = (String)body.get("name");

		if (Validator.isNull(name)) {
			throw new BadRequestException("Invalid body");
		}

		Try<DLFolder> dlFolderTry = Try.fromFallible(
			() -> _dlFolderService.getFolder(
				groupIdLongIdentifier.getId(), parentFolderId, name));

		if (dlFolderTry.isSuccess()) {
			throw new BadRequestException(
				"A folder with that name already exists");
		}

		String description = (String)body.get("description");

		if (Validator.isNull(description)) {
			throw new BadRequestException("Invalid body");
		}

		dlFolderTry = Try.fromFallible(
			() -> _dlFolderService.addFolder(
				groupIdLongIdentifier.getId(), groupIdLongIdentifier.getId(),
				false, parentFolderId, name, description,
				new ServiceContext()));

		return dlFolderTry.getUnchecked();
	}

	private void _deleteDLFolder(LongIdentifier dlFolderIdLongIdentifier) {
		try {
			_dlFolderService.deleteFolder(dlFolderIdLongIdentifier.getId());
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private DLFolder _getDLFolder(LongIdentifier dlFolderIdLongIdentifier) {
		try {
			return _dlFolderService.getFolder(dlFolderIdLongIdentifier.getId());
		}
		catch (NoSuchEntryException | PrincipalException e) {
			throw new NotFoundException(
				"Unable to get folder " + dlFolderIdLongIdentifier.getId(), e);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private Optional<Group> _getGroupOptional(DLFolder dlFolder) {
		try {
			return Optional.of(
				_groupLocalService.getGroup(dlFolder.getGroupId()));
		}
		catch (NoSuchGroupException nsge) {
			throw new NotFoundException(nsge);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private PageItems<DLFolder> _getPageItems(
		Pagination pagination, LongIdentifier groupIdLongIdentifier) {

		try {
			List<DLFolder> dlFolders = _dlFolderService.getFolders(
				groupIdLongIdentifier.getId(), 0, pagination.getStartPosition(),
				pagination.getEndPosition(), null);
			int count = _dlFolderService.getFoldersCount(
				groupIdLongIdentifier.getId(), 0);

			return new PageItems<>(dlFolders, count);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private String _getPath(DLFolder dlFolder) {
		try {
			return dlFolder.getPath();
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private DLFolder _updateDLFolder(
		LongIdentifier dlFolderIdLongIdentifier, Map<String, Object> body) {

		DLFolder dlFolder = _getDLFolder(dlFolderIdLongIdentifier);

		String name = (String)body.get("name");
		String description = (String)body.get("description");

		if (Validator.isNull(name) || Validator.isNull(description)) {
			throw new BadRequestException("Invalid body");
		}

		Try<DLFolder> dlFolderTry = Try.fromFallible(
			() -> _dlFolderService.updateFolder(
				dlFolderIdLongIdentifier.getId(), dlFolder.getParentFolderId(),
				name, description, dlFolder.getDefaultFileEntryTypeId(),
				new ArrayList<>(), dlFolder.getRestrictionType(),
				new ServiceContext()));

		return dlFolderTry.getUnchecked();
	}

	@Reference
	private DLFolderService _dlFolderService;

	@Reference
	private GroupLocalService _groupLocalService;

}