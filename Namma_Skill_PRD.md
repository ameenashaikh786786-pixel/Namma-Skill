# Product Requirements Document (PRD)

**Project Title**: Android App Development using GenAI - Namma-Skill (Self-Employment)  
**Candidate Name**: Ameena Mohammed Zahoor Shaikh  
**USN**: 2VD22CS007  
**College**: KLS Vishwanathrao Deshpande Institute of Technology, Haliyal

---

# Namma-Skill: Product Requirements Document (PRD)

## 1. Executive Summary
**Namma-Skill** is a specialized vocational training gateway designed for rural youth in Karnataka and surrounding regions. The platform bridges the information gap between vocational training centers and potential candidates, offering a curated, aspirational, and easy-to-use interface for career development.

## 2. Product Vision & Goals
- **Vision**: To be the primary digital bridge for rural vocational empowerment.
- **Goals**: 
    - Reduce the friction in finding verified vocational courses.
    - Increase the enrollment rates for "Job Guaranteed" training programs.
    - Provide a transparent application tracking system for students.

## 3. Target Audience
- **Primary**: Rural youth (Ages 18-30) seeking employment and skill development.
- **Secondary**: Vocational training institutes and regional skill development centers.

## 4. Key Features
### 4.1 Course Discovery Engine
- **Trade-Based Filtering**: Users can filter courses by industry (e.g., Electrician, Tailoring, Coding).
- **Duration Categories**: Distinction between Short-term (3 months) and Long-term (1 year) courses.
- **Job Guarantee Badge**: Visual highlight for courses that offer placement assurance.

### 4.2 Training Center Locator
- **Map Integration**: Real-time location of centers using Google Maps.
- **Direct Interaction**: One-tap buttons for "Call Center" and "Get Directions".
- **District Filters**: Ability to search for centers in specific districts (Mysuru, Mandya, Bengaluru, etc.).

### 4.3 Application & Profile System
- **Profile Completion Tracking**: Visual progress bar for user profile data.
- **Candidate Summary**: A verification step before final application to ensure data accuracy.
- **Application History**: A persistent record of all courses applied for, synced with Firebase.

### 4.4 Aspirational Content
- **Success Stories**: A dedicated module showcasing alumni success to inspire new candidates.
- **Modern UI**: A premium, student-friendly interface using modern design tokens.

## 5. Technical Specifications
- **Mobile Platform**: Native Android (Kotlin).
- **Backend Infrastructure**: Firebase Suite (Authentication, Firestore, Cloud Functions).
- **Location Services**: Google Maps SDK for Android.
- **Real-time Sync**: Firestore snapshots for instant application status updates.

## 6. User Flow
1. **Login/Register**: Secure email authentication.
2. **Explore**: Browse the vocational catalog with smart filters.
3. **Select**: View detailed course and center information.
4. **Apply**: Review profile details and submit application.
5. **Track**: Monitor application status in the "My Profile" section.

## 7. Roadmap
- **Phase 1**: Vocational catalog and basic application flow (Completed).
- **Phase 2**: Real-time notifications and call center integration (Completed).
- **Phase 3**: Placement tracking and alumni community features (Upcoming).
