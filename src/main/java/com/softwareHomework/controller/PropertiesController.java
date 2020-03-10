package com.softwareHomework.controller;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.softwareHomework.Impl.PropertiesImpl;
import com.softwareHomework.model.PostRequestBody;
import com.softwareHomework.model.Properties;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/properties")
public class PropertiesController {

	private final String apiKey = "cs4783FTW";

	private final Logger LOGGER = LogManager.getLogger(PropertiesController.class);

	@Autowired
	private PropertiesImpl propertiesImpl;

	@GetMapping
	@ApiOperation(value = "Get all properties",
	notes = "This endpoint gets all the  property from the property database")
	
	public ResponseEntity<List<Properties>> getproperties(){

		LOGGER.info("Properties List Requested");

		List<Properties> properties = propertiesImpl.findAll();

		if(properties.size() > 1){

			LOGGER.info("Data retrieved from database succesfuly");

			return new ResponseEntity<List<Properties>>(properties, HttpStatus.OK);
		}

		LOGGER.info("Result not found");
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping
	public ResponseEntity<String> postProperties(@RequestHeader String apiAuth, @RequestBody PostRequestBody properties){

		JSONObject jsonObject = new JSONObject();

		if(!apiAuth.equals(apiKey)){

			jsonObject.put("message", "Invalid api key");
			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.FORBIDDEN);
		}
		LOGGER.info(properties.toString());

		String address = properties.getAddress();
		String city = properties.getCity();
		String state = properties.getState();
		String zip = properties.getZip();

		if(address.length() > 200){


			jsonObject.put("Message", "Address is more than 200 characters");

			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.NOT_FOUND);
		}

		if(city.length() > 50){

			jsonObject.put("Message", "City is more than 50 characters");

			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.NOT_FOUND);
		}

		if(state.length() != 2){


			jsonObject.put("Message", "State is more than less than 2 characters");

			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.NOT_FOUND);
		}

		if(zip.length() > 5 || zip.length() > 10 ){

			jsonObject.put("Message", "Zip is not within range characters");
			System.out.println(jsonObject.toString());

			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.NOT_FOUND);
		}


		int insert = propertiesImpl.insertIntoProperties(address, city, state, zip);

		if(insert == 1){//ensure insert query worked
			jsonObject.put("message", "added");
		}

		LOGGER.info("Item posted succesfully");
		return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.OK);

	}

	@GetMapping("/{id}")
	@ApiOperation(value = "Find property by id",
	notes = "Provide and ID to look up a specific property from the property database",
	response = Properties.class)
	public ResponseEntity<Properties> getPropertyByID(@ApiParam(value = "ID of property to retrieve", required = true) 
	@PathVariable String id){

		if(!id.isEmpty()){

			try{

				Integer.parseInt(id);

			}catch (NumberFormatException ex){

				LOGGER.error("Invalid URI Parameter, Enter a valid Number");
				return new ResponseEntity<Properties>(HttpStatus.BAD_REQUEST);
			}

		}

		LOGGER.info("Property ID requested = + " + id);


		try{

			Optional<Properties> optionalProperty = propertiesImpl.findById(Integer.parseInt(id));
			Properties properties = optionalProperty.get();

			if(properties.getAddress() != null){

				return new ResponseEntity<Properties>(properties, HttpStatus.OK);

			}


			return new ResponseEntity<Properties>(HttpStatus.NOT_FOUND);

		}catch(Exception ex){

			LOGGER.error("User with specified ID not found");
			return new ResponseEntity<Properties>(HttpStatus.NOT_FOUND);
		}

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deletePropertyByID(@RequestHeader String apiAuth, 
													@ApiParam(value = "ID of property to retrieve", required = true) @PathVariable String id){

		JSONObject jsonObject = new JSONObject();

		if(!id.isEmpty()){

			try{

				Integer.parseInt(id);

			}catch (NumberFormatException ex){

				LOGGER.error("Invalid URI Parameter, Enter a valid Number");
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}

		}

		if(!apiAuth.equals(apiKey) || apiAuth == null || apiAuth.isEmpty()){

			jsonObject.put("message", "Invalid api key");
			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.FORBIDDEN);
		}


		try{
			
			propertiesImpl.deleteById(Integer.parseInt(id));
			
		}catch(Exception ex){

			jsonObject.put("message", "Unable to delete property with the specified ID");
			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.NOT_FOUND);

		}

		jsonObject.put("message", "Property deleted");
		return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<String> updatePropertyByID(@RequestHeader String apiAuth, @ApiParam(value = "ID of property to retrieve", required = true) 
													@PathVariable String id, @RequestBody PostRequestBody properties){

		JSONObject jsonObject = new JSONObject();

		if(!id.isEmpty()){

			try{

				Integer.parseInt(id);

			}catch (NumberFormatException ex){

				LOGGER.error("Invalid URI Parameter, Enter a valid Number");
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}

		}

		if(!apiAuth.equals(apiKey) || apiAuth == null || apiAuth.isEmpty()){

			jsonObject.put("message", "Invalid api key");
			return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.UNAUTHORIZED);
		}

		try{

			Optional<Properties> properties2 = propertiesImpl.findById(Integer.valueOf(id));

			Properties tempProperties = properties2.get();

			tempProperties.setAddress(properties.getAddress());
			tempProperties.setCity(properties.getCity());
			tempProperties.setState(properties.getState());
			tempProperties.setZip(properties.getZip());

			Properties updatedProperties = propertiesImpl.update(tempProperties);

		}catch (Exception ex){

			LOGGER.error("Record ID not found to update");
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}

		jsonObject.putOnce("message", "Property updated");

		return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.OK);

	}

}
