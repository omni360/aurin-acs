# Conversion API

Because the ACS is designed to do only one thing (convert files to C3ML), it has a very simple API.

To convert a file, send an HTTP `POST` request to the ACS server with the path `/convert`.

It is assumed that the client application will be uploading a file to convert via an HTML form with
multipart file encoding (to set the `Content-Type` header of the request) and a file input field
named `file`, such as: 

    <form enctype="multipart/form-data">
        <input type="file" name="file" />
        <input type="submit" value="Import"/>
    </form>

The C3ML response will be JSON, so the request may optionally specifiy the header
`Accepts: application/json`. Since the response is *not* HTML, the form should be submitted
via AJAX (e.g. with jQuery's [`$.post`][jquery] method) rather than directly with the submit button.

That's all there is to it! ACS will determine the type of the uploaded file based on its extension
(e.g. `.json` for GeoJSON or `.kmz` for KMZ), convert it to C3ML and return the results
synchronously to simplify logic on both the client and server.

Here is an example request:

    POST http://acs.aurin.org.au/convert
    Content-Type:   multipart/form-data
    Accept:         application/json
    
    file=<binary file data>

And response:

    {...}

For more details on the structure of the response, refer to the [Design of C3ML](design.md#c3ml).


[jquery]: https://api.jquery.com/jquery.post/