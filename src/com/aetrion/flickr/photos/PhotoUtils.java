package com.aetrion.flickr.photos;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.aetrion.flickr.people.User;
import com.aetrion.flickr.util.XMLUtilities;
import com.aetrion.flickr.tags.Tag;

/**
 * Utilitiy-methods to transfer requested XML to Photo-objects.
 * 
 * @author till, x-mago
 * @version $Id: PhotoUtils.java,v 1.2 2007/02/23 23:04:01 x-mago Exp $
 */
public class PhotoUtils {

	private PhotoUtils() {
	}
	
	public static final Photo createPhoto(Element photoElement) {		
        Photo photo = new Photo();
        photo.setId(photoElement.getAttribute("id"));
        photo.setSecret(photoElement.getAttribute("secret"));
        photo.setServer(photoElement.getAttribute("server"));
        photo.setFarm(photoElement.getAttribute("farm"));
        photo.setFavorite("1".equals(photoElement.getAttribute("isfavorite")));
        photo.setLicense(photoElement.getAttribute("license"));
        photo.setOriginalFormat(photoElement.getAttribute("originalformat"));
        
        Element ownerElement = (Element) photoElement.getElementsByTagName("owner").item(0);
        User owner = new User();
        owner.setId(ownerElement.getAttribute("nsid"));

        String username = ownerElement.getAttribute("username");
        String ownername = ownerElement.getAttribute("ownername");
        // try to get the username either from the "username" attribute or
        // from the "ownername" attribute 
        if (username != null && !"".equals(username)) {
            owner.setUsername(username);
        } else if (ownername != null && !"".equals(ownername)) {
        	owner.setUsername(ownername);
        }
        
        owner.setUsername(ownerElement.getAttribute("username"));
        owner.setRealName(ownerElement.getAttribute("realname"));
        owner.setLocation(ownerElement.getAttribute("location"));
        photo.setOwner(owner);

        photo.setTitle(XMLUtilities.getChildValue(photoElement, "title"));
        photo.setDescription(XMLUtilities.getChildValue(photoElement, "description"));

        Element visibilityElement = (Element) photoElement.getElementsByTagName("visibility").item(0);
        photo.setPublicFlag("1".equals(visibilityElement.getAttribute("ispublic")));
        photo.setFriendFlag("1".equals(visibilityElement.getAttribute("isfriend")));
        photo.setFamilyFlag("1".equals(visibilityElement.getAttribute("isfamily")));

        Element datesElement = XMLUtilities.getChild(photoElement, "dates");
        photo.setDatePosted(datesElement.getAttribute("posted"));
        photo.setDateTaken(datesElement.getAttribute("taken"));
        photo.setTakenGranularity(datesElement.getAttribute("takengranularity"));

        NodeList permissionsNodes = photoElement.getElementsByTagName("permissions");
        if (permissionsNodes.getLength() > 0) {
            Element permissionsElement = (Element) permissionsNodes.item(0);
            Permissions permissions = new Permissions();
            permissions.setComment(permissionsElement.getAttribute("permcomment"));
            permissions.setAddmeta(permissionsElement.getAttribute("permaddmeta"));
        }

        Element editabilityElement = (Element) photoElement.getElementsByTagName("editability").item(0);
        Editability editability = new Editability();
        editability.setComment("1".equals(editabilityElement.getAttribute("cancomment")));
        editability.setAddmeta("1".equals(editabilityElement.getAttribute("canaddmeta")));

        Element commentsElement = (Element) photoElement.getElementsByTagName("comments").item(0);
        photo.setComments(((Text) commentsElement.getFirstChild()).getData());

        Element notesElement = (Element) photoElement.getElementsByTagName("notes").item(0);
        List notes = new ArrayList();
        NodeList noteNodes = notesElement.getElementsByTagName("note");
        for (int i = 0; i < noteNodes.getLength(); i++) {
            Element noteElement = (Element) noteNodes.item(i);
            Note note = new Note();
            note.setId(noteElement.getAttribute("id"));
            note.setAuthor(noteElement.getAttribute("author"));
            note.setAuthorName(noteElement.getAttribute("authorname"));
            note.setBounds(noteElement.getAttribute("x"), noteElement.getAttribute("y"),
                noteElement.getAttribute("w"), noteElement.getAttribute("h"));
            note.setText(noteElement.getTextContent());
            notes.add(note);
        }
        photo.setNotes(notes);

        Element tagsElement = (Element) photoElement.getElementsByTagName("tags").item(0);
        List tags = new ArrayList();
        NodeList tagNodes = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagNodes.getLength(); i++) {
            Element tagElement = (Element) tagNodes.item(i);
            Tag tag = new Tag();
            tag.setId(tagElement.getAttribute("id"));
            tag.setAuthor(tagElement.getAttribute("author"));
            tag.setRaw(tagElement.getAttribute("raw"));
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        photo.setTags(tags);

        Element urlsElement = (Element) photoElement.getElementsByTagName("urls").item(0);
        List urls = new ArrayList();
        NodeList urlNodes = urlsElement.getElementsByTagName("url");
        for (int i = 0; i < urlNodes.getLength(); i++) {
            Element urlElement = (Element) urlNodes.item(i);
            PhotoUrl photoUrl = new PhotoUrl();
            photoUrl.setType(urlElement.getAttribute("type"));
            photoUrl.setUrl(XMLUtilities.getValue(urlElement));
            if (photoUrl.getType().equals("photopage")) {
                photo.setUrl(photoUrl.getUrl());
            }
        }
        photo.setUrls(urls);
        photo.setUrl("http://flickr.com/photos/" + owner.getId() + "/" + photo.getId());

        Element geoElement = (Element) photoElement.getElementsByTagName("location").item(0);
        String longitude = geoElement.getAttribute("longitude");
        String latitude = geoElement.getAttribute("latitude");
        String accuracy = geoElement.getAttribute("accuracy");
     
        if (longitude != null && latitude != null 
        		&& longitude.length() > 0 && latitude.length() > 0
        		&& !("0".equals(longitude) && "0".equals(latitude))) {
        	photo.setGeoData(new GeoData(longitude, latitude, accuracy));
        }
        
        return photo;
	}
	
	public static final PhotoList createPhotoList(Element photosElement) {
		PhotoList photos = new PhotoList();
    	photos.setPage(photosElement.getAttribute("page"));
    	photos.setPages(photosElement.getAttribute("pages"));
    	photos.setPerPage(photosElement.getAttribute("perpage"));
    	photos.setTotal(photosElement.getAttribute("total"));

    	NodeList photoNodes = photosElement.getElementsByTagName("photo");
    	for (int i = 0; i < photoNodes.getLength(); i++) {
    		Element photoElement = (Element) photoNodes.item(i);
    		photos.add(PhotoUtils.createPhoto(photoElement));
    	}
    	return photos;
	}

}