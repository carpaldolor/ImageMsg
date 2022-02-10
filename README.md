# ImageMsg 
A Crypto-Steganography tool designed for Cyber Security Researchers to create .PNG files with encrypted steno payloads for testing detection tools.  It uses the LSB from all 3 color palates and pads unused space with random bits.  The contents of the payload are encypted with AES-256.

For best results create a PNG carrier by conversion from a low quality JPG.  This will contain a good base line LSB bit distribution.

Create the test PNG image from this and compare the results.


#Execution


Encode a small message:

Usage: java -jar ImageMsg.jar -encode -in <image.png> -out <new_image.png> -key "abc123xyz000secret000password" -msg "This is a secret message"


Encode a File:


Usage: java -jar ImageMsg.jar -encode -in <image.png> -out <new_image.png> -keyfile key.txt -msgfile message.txt 


Decode :

Usage: java -jar ImageMsg.jar -decode -in <new_image.png> -key "abc123xyz000secret000password" 

Usage: java -jar ImageMsg.jar -decode -in <new_image.png> -keyfile key.txt 

