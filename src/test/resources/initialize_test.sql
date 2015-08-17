-- =============================================================================================
-- Now for the studies
DROP TABLE IF EXISTS "STUDIES";

CREATE TABLE "STUDIES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "NAME" VARCHAR(24) NOT NULL,
  "DESCRIPTION" VARCHAR(2048),
  "IDENTIFIER_ATTRIBUTE_ID" INTEGER,
  UNIQUE ("NAME")
);

INSERT INTO "STUDIES" ("ID", "NAME", "DESCRIPTION", "IDENTIFIER_ATTRIBUTE_ID") VALUES (1, 'DEMO', 'A demo clinical genomics study', 2);
INSERT INTO "STUDIES" ("ID", "NAME", "DESCRIPTION", "IDENTIFIER_ATTRIBUTE_ID") VALUES (2, 'SECOND', 'A second study', 2);

-- =============================================================================================
-- Now for the attributes

DROP TABLE IF EXISTS "ATTRIBUTES";

CREATE TABLE "ATTRIBUTES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "STUDY_ID" INTEGER NOT NULL, 
  "NAME" VARCHAR(48) NOT NULL, 
  "DESCRIPTION" VARCHAR(2048), 
  "LABEL" VARCHAR(128) NOT NULL, 
  "TYPE" VARCHAR(24) NOT NULL, 
  "RANK" INTEGER DEFAULT 0 NOT NULL, 
  "OPTIONS" VARCHAR(2048),
  UNIQUE("STUDY_ID", "NAME")
);

INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (1, 1, 1, 'dateEntered', 'Date Entered', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (2, 2, 1, 'patientId', 'Patient ID', 'string', '{"unique":true,"display":"pin_left"}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (3, 3, 1, 'mrn', 'MRN', 'string', '{"tags": ["identifiable"]}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (4, 4, 1, 'primarySite', 'Primary Disease Site', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (5, 5, 1, 'consentDate', 'Date of Consent', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (6, 6, 1, 'physician', 'Treating Physician', 'string', '{"tags": ["identifiable"]}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (7, 7, 1, 'sampleAvailable', 'Sample Available At (Hospital/Lab)', 'option', '{"values": ["LMP","St. Michaels","Toronto East General"]}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (8, 8, 1, 'specimenNo', 'Specimen #', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (9, 9, 1, 'procedureDate', 'Date of Procedure', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (10, 10, 1, 'tissueSite', 'Tissue Site', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (11, 11, 1, 'trackerDate', 'Date Internal Request Entered in Tracker', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (12, 12, 1, 'specimenAvailable', 'Biobank Specimen Available? (Yes/No)', 'boolean');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (13, 13, 1, 'biobankDate', 'Date Column M (Biobank Yes/No) Entered', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (14, 14, 1, 'biobankReason', 'Reason Tissue Not Banked', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (15, 15, 1, 'requestDate', 'Date Request Processed', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (16, 16, 1, 'LMPComments', 'LMP Comments', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (17, 17, 1, 'blockLocation', 'Current Block Location', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (18, 18, 1, 'CSPDate', 'Date Specimen Received by CSP', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (19, 19, 1, 'specimenBiobankDate', 'Date Specimen went to Biobank', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (20, 20, 1, 'specimenType', 'Specimen Type Collected by CSP (block/slides)', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (21, 21, 1, 'diagnosticsDate', 'Date of transfer to Molecular Diagnostics', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (22, 22, 1, 'returnRequested', 'Return Requested? (Yes/No)', 'boolean');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (23, 23, 1, 'returnDate', 'Date of Return to Outside Institution', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (24, 24, 1, 'bloodCollDate', 'Date and Time of Blood Collection', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (25, 25, 1, 'insufficientDate', 'Date Tissue Deemed Insufficient', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (26, 26, 1, 'notes', 'Notes', 'string', '{"longtext":true}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (27, 27, 1, 'study', 'Study', 'string');

INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (28, 1, 2, 'patientId', 'Patient ID', 'string');

-- =============================================================================================
-- Now for the views

DROP TABLE IF EXISTS "VIEWS";

CREATE TABLE "VIEWS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, 
  "STUDY_ID" INTEGER NOT NULL, 
  "NAME" VARCHAR(24) NOT NULL, 
  "DESCRIPTION" VARCHAR(2048), 
  "OPTIONS" VARCHAR(2048),
  UNIQUE ("STUDY_ID", "NAME")
);

INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME") VALUES (1, 1, 'Manages the whole study', 'complete');
INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME") VALUES (2, 1, 'Tracks the study', 'track');
INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME", "OPTIONS") VALUES (3, 1, 'Tracks only secondary', 'secondary', '{"rows":[{"attribute":"study","value":"secondary"}]}');

INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME") VALUES (4, 2, 'Manages the whole study', 'complete');

-- =============================================================================================
-- Now for the view attributes

DROP TABLE IF EXISTS "VIEW_ATTRIBUTES";

CREATE TABLE "VIEW_ATTRIBUTES" (
  "VIEW_ID" INTEGER NOT NULL, 
  "ATTRIBUTE_ID" INTEGER NOT NULL, 
  "RANK" INTEGER DEFAULT 0 NOT NULL,
  "OPTIONS" VARCHAR(2048), 
  PRIMARY KEY ("VIEW_ID", "ATTRIBUTE_ID")
);

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (1, 1, 1, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (1, 2, 2, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 3, 3);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 4, 4);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 5, 5);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 6, 6);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 7, 7);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 8, 8);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 9, 9);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 10, 10);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 11, 11);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 12, 12);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 13, 13);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 14, 14);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 15, 15);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 16, 16);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 17, 17);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 18, 18);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 19, 19);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 20, 20);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 21, 21);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 22, 22);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 23, 23);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 24, 24);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 25, 25);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 26, 26);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 27, 27);

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 1, 1);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 2, 2);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 5, 5);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 6, 6);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 10, 10);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 12, 12);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 16, 16);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 19, 19);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 24, 24);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 25, 25);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 26, 26);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 27, 27);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (2, 14, 14, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (2, 20, 20, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (2, 22, 22, '{"classes": ["label5"]}');

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 1, 1);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 2, 2);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 5, 5);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 6, 6);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 10, 10);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 12, 12);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 16, 16);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 19, 19);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 24, 24);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 25, 25);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 26, 26);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 27, 27);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (3, 14, 14, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (3, 20, 20, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (3, 22, 22, '{"classes": ["label5"]}');

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (4, 28, 1);

-- =============================================================================================
-- Now for the cases

DROP TABLE IF EXISTS "CASES";

CREATE TABLE "CASES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, 
  "STUDY_ID" INTEGER NOT NULL, 
  "STATE" VARCHAR(255)
);

INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (1, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (2, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (3, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (4, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (5, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (6, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (7, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (8, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (9, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (10, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (11, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (12, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (13, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (14, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (15, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (16, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (17, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (18, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (19, 1);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (20, 1);

INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (21, 2);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (22, 2);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (23, 2);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (24, 2);
INSERT INTO "CASES" ("ID", "STUDY_ID") VALUES (25, 2);

-- =============================================================================================
-- Now for the case attribute values, of all types

DROP TABLE IF EXISTS "CASE_ATTRIBUTE_STRINGS";
DROP TABLE IF EXISTS "CASE_ATTRIBUTE_DATES";
DROP TABLE IF EXISTS "CASE_ATTRIBUTE_BOOLEANS";

CREATE TABLE "CASE_ATTRIBUTE_STRINGS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, 
  "CASE_ID" INTEGER NOT NULL, 
  "ATTRIBUTE" VARCHAR(24) NOT NULL, 
  "VALUE" VARCHAR(4096) COLLATE SQL_TEXT_UCC, 
  "NOT_AVAILABLE" BOOLEAN DEFAULT 0 NOT NULL, 
  "NOTES" VARCHAR(2048)
);

CREATE TABLE "CASE_ATTRIBUTE_DATES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, 
  "CASE_ID" INTEGER NOT NULL, 
  "ATTRIBUTE" VARCHAR(24) NOT NULL, 
  "VALUE" DATE, 
  "NOT_AVAILABLE" BOOLEAN DEFAULT 0 NOT NULL, 
  "NOTES" VARCHAR(2048)
);

CREATE TABLE "CASE_ATTRIBUTE_BOOLEANS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, 
  "CASE_ID" INTEGER NOT NULL, 
  "ATTRIBUTE" VARCHAR(24) NOT NULL, 
  "VALUE" BOOLEAN, 
  "NOT_AVAILABLE" BOOLEAN DEFAULT 0 NOT NULL, 
  "NOTES" VARCHAR(2048)
);

INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'dateEntered', '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'dateEntered', '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'dateEntered', '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'dateEntered', '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'dateEntered', '2014-08-21');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (6, 'dateEntered', '2014-08-22');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (7, 'dateEntered', '2014-08-22');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (8, 'dateEntered', '2014-08-23');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (9, 'dateEntered', '2014-08-23');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (10, 'dateEntered', '2014-08-24');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (11, 'dateEntered', '2014-08-25');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (12, 'dateEntered', '2014-08-26');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (13, 'dateEntered', '2014-08-28');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (14, 'dateEntered', '2014-08-28');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (15, 'dateEntered', '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (16, 'dateEntered', '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (17, 'dateEntered', '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (18, 'dateEntered', '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (19, 'dateEntered', '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (20, 'dateEntered', '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'patientId', 'DEMO-01');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'patientId', 'DEMO-02');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'patientId', 'DEMO-03');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'patientId', 'DEMO-03');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'patientId', 'DEMO-06');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (6, 'patientId', 'DEMO-05');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (7, 'patientId', 'DEMO-04');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (8, 'patientId', 'DEMO-07');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (9, 'patientId', 'DEMO-08');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (10, 'patientId', 'DEMO-09');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (11, 'patientId', 'DEMO-10');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (12, 'patientId', 'DEMO-11');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (13, 'patientId', 'DEMO-12');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (14, 'patientId', 'DEMO-13');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (15, 'patientId', 'DEMO-14');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (16, 'patientId', 'DEMO-15');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (17, 'patientId', 'DEMO-16');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (18, 'patientId', 'DEMO-17');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (19, 'patientId', 'DEMO-18');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (20, 'patientId', 'DEMO-19');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'mrn', '0101010');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'mrn', '0202020');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'mrn', '0303030');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'mrn', '0303030');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'mrn', '0404040');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'primarySite', 'breast');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'primarySite', 'gyne - ovarian');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'primarySite', 'lung');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'primarySite', 'lung');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'primarySite', 'pancreatobiliary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'physician', 'Dr. X');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'physician', 'Dr. Y');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'physician', 'Dr. Z');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'physician', 'Dr. Z');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'physician', 'Dr. W');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'sampleAvailable', 'LMP');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'sampleAvailable', 'St. Michael''s');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'sampleAvailable', 'LMP');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'sampleAvailable', 'Toronto East General');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'sampleAvailable', 'LMP');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'specimenNo', 'S14-1');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'specimenNo', 'S14-200');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'specimenNo', 'S12-3000');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'specimenNo', 'S12-400');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'specimenNo', 'S13-333');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'procedureDate', '2014-01-01');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'procedureDate', '2014-02-03');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'procedureDate', '2012-05-04');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'procedureDate', '2012-04-26');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'procedureDate', '2013-03-01');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'tissueSite', 'breast (left), lymph node');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'tissueSite', 'omentum');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'tissueSite', 'Lymph node');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'tissueSite', 'RLL lung');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'trackerDate', '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'trackerDate', '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'trackerDate', '2014-08-21');
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'specimenAvailable', 1);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'specimenAvailable', 0);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'specimenAvailable', 0);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'specimenAvailable', 0);
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'notes', 'This is an extremely long note, which should be able to wrap around and which ought to be clipped properly in the grid');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (1, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (2, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (3, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (4, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (5, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (6, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (7, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (8, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (9, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (10, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (11, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (12, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (13, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (14, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (15, 'study', 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (16, 'study', 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (17, 'study', 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (18, 'study', 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (19, 'study', 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (20, 'study', 'secondary');

INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "NOT_AVAILABLE") VALUES (5, 'tissueSite', 1);
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "NOT_AVAILABLE") VALUES (2, 'trackerDate', 1);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE", "NOT_AVAILABLE") VALUES (4, 'specimenAvailable', 1);

INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE", "NOTES") VALUES (1, 'consentDate', '2014-08-19', '{"locked" : true, "tags": ["label1"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE", "NOTES") VALUES (2, 'consentDate', '2014-08-18', '{"locked" : true, "tags": ["label1"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE", "NOTES") VALUES (3, 'consentDate', '2014-08-19', '{"locked" : true, "tags": ["label1"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE", "NOTES") VALUES (4, 'consentDate', '2014-08-19', '{"locked" : true, "tags": ["label1"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE", "VALUE", "NOTES") VALUES (5, 'consentDate', '2014-08-20', '{"locked" : true, "tags": ["label1"]}');

INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (21, 'patientId', 'SECOND-01');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (22, 'patientId', 'SECOND-02');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (23, 'patientId', 'SECOND-03');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (24, 'patientId', 'SECOND-04');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE", "VALUE") VALUES (25, 'patientId', 'SECOND-05');

-- =============================================================================================
-- Now for the audit log

DROP TABLE IF EXISTS "AUDIT_LOG";

CREATE TABLE "AUDIT_LOG" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "STUDY_ID" INTEGER NOT NULL, 
  "CASE_ID" INTEGER NOT NULL, 
  "ATTRIBUTE" VARCHAR(24) NOT NULL, 
  "EVENT_TIME" TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
  "EVENT_USER" VARCHAR(128) NOT NULL, 
  "EVENT_TYPE" VARCHAR(12) NOT NULL, 
  "EVENT_ARGS" VARCHAR(2048)
);

-- =============================================================================================
-- Now for the users

DROP TABLE IF EXISTS "USER_ROLES";
DROP TABLE IF EXISTS "ROLE_PERMISSIONS";
DROP TABLE IF EXISTS "ROLES";
DROP TABLE IF EXISTS "USERS";

CREATE TABLE "USERS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "USERNAME" VARCHAR(64) NOT NULL, 
  "HASH" VARCHAR(64),
  "EMAIL" VARCHAR(256),
  "APIKEY" VARCHAR(64),
  "LOCKED" BOOLEAN DEFAULT 0 NOT NULL, 
  "FORCE_PASSWORD_CHANGE" BOOLEAN DEFAULT 0 NOT NULL, 
  "EXPIRES" DATE
);

INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (1, 'admin', '$2a$10$iiwt9.KFFYLHaIJTrhMH..xIduQS2jSJAcj.7v123prhYG7UgL54.');
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (2, 'anca', '$2a$10$r0PaPczi7J8YKL3kOJu4FemQzWM/qUG94po81CxgCBE/pN/hikz2W');
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (3, 'aaron', '$2a$10$b3dQzerwzZhvcM9p1xB9H.ugoyjLJwBK1bhPu9TVQp1zmXLgk/UIq');
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (4, 'stuart', '$2a$10$axuMX9WrGc4j6q0ifC06K.fv1L7wrfKH5RTrHycp7FqjKJxrKTvde');
INSERT INTO "USERS" ("ID", "USERNAME", "APIKEY") VALUES (5, 'medidata', 'JHzhM9EI18flp7l540wtaRz1z3d4689u');

-- Test with susan 5fx/FG1a -- generated at: http://aspirine.org/htpasswd_en.html
-- Note signature patched to $2a -- we'll add this to the core code
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (6, 'susan', '$2y$11$gSosE7GOkfL/j8SwYGPnBe0WnRWypxlxlsxWe0towOGLp2mIaLK.6');

CREATE TABLE "ROLES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "NAME" VARCHAR(64) NOT NULL, 
  UNIQUE ("NAME")
);

INSERT INTO "ROLES" ("ID", "NAME") VALUES (1, 'ROLE_ADMIN');
INSERT INTO "ROLES" ("ID", "NAME") VALUES (2, 'ROLE_DEMO_TRACK');
INSERT INTO "ROLES" ("ID", "NAME") VALUES (3, 'ROLE_DEMO_ADMIN');
INSERT INTO "ROLES" ("ID", "NAME") VALUES (4, 'ROLE_DEMO_READ');

CREATE TABLE "USER_ROLES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "USERNAME" VARCHAR(64) NOT NULL, 
  "ROLE_ID" INTEGER NOT NULL REFERENCES "ROLES"("ID")
);

INSERT INTO "USER_ROLES" ("ID", "USERNAME", "ROLE_ID") VALUES (1, 'morungos@gmail.com', 1);
INSERT INTO "USER_ROLES" ("ID", "USERNAME", "ROLE_ID") VALUES (2, 'stuart', 2);
INSERT INTO "USER_ROLES" ("ID", "USERNAME", "ROLE_ID") VALUES (3, 'anca', 3);
INSERT INTO "USER_ROLES" ("ID", "USERNAME", "ROLE_ID") VALUES (4, 'morag', 4);
INSERT INTO "USER_ROLES" ("ID", "USERNAME", "ROLE_ID") VALUES (5, 'stuartw@ads.uhnresearch.ca', 1);
INSERT INTO "USER_ROLES" ("ID", "USERNAME", "ROLE_ID") VALUES (6, 'oidcprofile#stuartw', 1);

CREATE TABLE "ROLE_PERMISSIONS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "ROLE_ID" INTEGER NOT NULL REFERENCES "ROLES"("ID"),
  "PERMISSION" VARCHAR(64) NOT NULL
);

INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (1, 1, '*');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (2, 3, 'study:*:DEMO');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (3, 2, 'study:read:DEMO');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (4, 2, 'view:read:DEMO-track');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (5, 2, 'view:write:DEMO-track');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (6, 4, 'study:read:DEMO');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (7, 4, 'view:read:DEMO-track');
