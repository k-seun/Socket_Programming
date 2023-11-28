#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN 10
#define RST_PIN 9
#define BUTTON_PIN 2  // 버튼 핀 번호

MFRC522 rfid(SS_PIN, RST_PIN); // RFID 인스턴스
MFRC522::MIFARE_Key key; 

byte nuidPICC[4];

void setup() { 
  Serial.begin(9600);
  SPI.begin();
  rfid.PCD_Init();

  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }

  pinMode(BUTTON_PIN, INPUT_PULLUP);  // 버튼 핀을 입력 모드로 설정

  Serial.println(F("This code scans the RFID card NUID."));
  Serial.print(F("Using the following key:"));
  printHex(key.keyByte, MFRC522::MF_KEY_SIZE);
}
 
void loop() {
  if (!rfid.PICC_IsNewCardPresent())
    return;

  if (!rfid.PICC_ReadCardSerial())
    return;

  Serial.print(F("Card type: "));
  MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
  Serial.println(rfid.PICC_GetTypeName(piccType));

  Serial.println(F("A new card has been detected."));

  for (byte i = 0; i < 4; i++) {
    nuidPICC[i] = rfid.uid.uidByte[i];
  }
 
  Serial.println(F("The NUID tag is:"));
  Serial.print(F("In hex: "));
  printHex(rfid.uid.uidByte, rfid.uid.size);
  Serial.println();
  Serial.print(F("In dec: "));
  printDec(rfid.uid.uidByte, rfid.uid.size);
  Serial.println();

  // Check if the button is pressed
  if (digitalRead(BUTTON_PIN) == HIGH) {
    // Button is pressed, treat it as item checkout
    Serial.println(F("Item checkout"));
    // Add your code to store the checkout information in the database
  } else {
    // Button is not pressed, treat it as item checkin
    Serial.println(F("Item checkin"));
    // Add your code to store the checkin information in the database
  }

  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();
}

void printHex(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}

void printDec(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], DEC);
  }
}
