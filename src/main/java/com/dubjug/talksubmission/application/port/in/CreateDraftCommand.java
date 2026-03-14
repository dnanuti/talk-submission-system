package com.dubjug.talksubmission.application.port.in;

public record CreateDraftCommand(String title, String abstractText, String speakerName) {
}
