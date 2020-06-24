package com.sap.cap.sample.processors;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cap.sample.managers.StoreServiceManager;
import com.sap.cap.sample.utils.MessageKeys;
import com.sap.cap.sample.utils.ResponseHelper;
import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;

import cds.gen.storeservice.Books;
import cds.gen.storeservice.StoreService_;

@Component
@ServiceName(StoreService_.CDS_NAME)
public class StoreServiceProcesor implements EventHandler {

	@Autowired
	private StoreServiceManager storageServiceManager;

	@Autowired
	private ResponseHelper response;

	@Before(event = CdsService.EVENT_CREATE)
	public void beforeCreate(Books book) {

		/* (If provided) Check if author exists else through error */
		if (book.getAuthor() != null) {
			if (storageServiceManager.readAuthorByName(book.getAuthor().getName()) != null) {
				response.conflict(MessageKeys.AUTHOR_ALREADY_EXISTS, book.getAuthor().getName());
			}
		}

		/* We can always overwrite the default values */
		book.setCreatedBy("membershipId");
		book.setModifiedBy("membershipId");

	}

	@Before(event = { CdsService.EVENT_UPDATE, CdsService.EVENT_UPSERT, CdsService.EVENT_DELETE })
	public void beforeUpdate(Books book) {
		book.setModifiedBy("membershipId");
	}

	@After(event = CdsService.EVENT_READ)
	public void afterRead(Stream<Books> books) {

		Optional<Books> result = books//
				.filter(b -> b.getAuthor().getName().equalsIgnoreCase("lav"))//
				.findAny();

		if (result.get() != null) {
			response.forbidden(MessageKeys.NO_ACCESS_TO_LAV_BOOKS);
		}
	}

}