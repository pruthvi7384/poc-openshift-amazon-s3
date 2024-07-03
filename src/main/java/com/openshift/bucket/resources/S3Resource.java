package com.openshift.bucket.resources;

import com.openshift.bucket.models.Request;
import com.openshift.bucket.services.S3Service;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/s3")
public class S3Resource {

    @Inject
    S3Service s3Service;

    @POST()
    @Path("/create-and-upload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createAndUploadFile(Request request){
        com.openshift.bucket.models.Response response = s3Service.createAndUploadFileService(request);
        return Uni.createFrom().item(() -> Response.status(Response.Status.OK).entity(response).build());
    }

}
