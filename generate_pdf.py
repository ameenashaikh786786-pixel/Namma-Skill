from fpdf import FPDF

class PRD_PDF(FPDF):
    def header(self):
        if self.page_no() > 1:
            self.set_font('Helvetica', 'B', 12)
            self.set_text_color(33, 150, 243)
            self.cell(0, 10, 'Namma-Skill: Product Requirements Document', ln=True, align='R')
            self.line(10, 20, 200, 20)
            self.ln(10)

    def footer(self):
        self.set_y(-15)
        self.set_font('Helvetica', 'I', 8)
        self.set_text_color(128)
        self.cell(0, 10, f'Page {self.page_no()}', align='C')

    def cover_page(self):
        self.add_page()
        self.set_y(50) 
        self.set_font('Helvetica', 'B', 22)
        self.set_text_color(33, 150, 243)
        self.multi_cell(0, 12, 'PRODUCT REQUIREMENTS DOCUMENT\n(PRD)', align='C')
        
        self.ln(8) 
        self.set_font('Helvetica', 'B', 16)
        self.set_text_color(0, 0, 0)
        self.multi_cell(0, 10, 'Android App Development using GenAI -\nNamma-Skill (Self-Employment)', align='C')
        
        self.ln(20) # Use ln instead of set_y to keep things relative and tight
        self.set_font('Helvetica', 'B', 12)
        self.cell(0, 8, 'Submitted By:', ln=True, align='C')
        self.set_font('Helvetica', '', 14)
        self.cell(0, 10, 'Ameena Mohammed Zahoor Shaikh', ln=True, align='C')
        self.set_font('Helvetica', 'B', 12)
        self.cell(0, 8, 'USN: 2VD22CS007', ln=True, align='C')
        
        self.ln(15) 
        self.set_font('Helvetica', 'B', 12)
        self.cell(0, 8, 'College:', ln=True, align='C')
        self.set_font('Helvetica', '', 12)
        self.multi_cell(0, 8, 'KLS Vishwanathrao Deshpande Institute of Technology,\nHaliyal', align='C')

    def chapter_title(self, label):
        self.set_font('Helvetica', 'B', 14)
        self.set_text_color(0, 0, 0)
        self.cell(0, 10, label, ln=True, align='L')
        self.ln(2)

    def chapter_body(self, text):
        self.set_font('Helvetica', '', 11)
        self.set_text_color(50, 50, 50)
        self.multi_cell(0, 6, text)
        self.ln(4)

    def add_bullet(self, text, indent=10):
        self.set_x(indent)
        self.set_font('Helvetica', '', 11)
        self.set_text_color(50, 50, 50)
        self.cell(5, 6, chr(149))
        self.multi_cell(0, 6, text)
        self.ln(1)

pdf = PRD_PDF()
pdf.set_auto_page_break(auto=True, margin=15)
pdf.cover_page()
pdf.add_page()

# 1. Executive Summary
pdf.chapter_title('1. Executive Summary')
pdf.chapter_body('Namma-Skill is a specialized vocational training gateway designed for rural youth in Karnataka and surrounding regions. The platform bridges the information gap between vocational training centers and potential candidates, offering a curated, aspirational, and easy-to-use interface for career development.')

# 2. Product Vision & Goals
pdf.chapter_title('2. Product Vision & Goals')
pdf.add_bullet('Vision: To be the primary digital bridge for rural vocational empowerment.')
pdf.add_bullet('Goals:')
pdf.add_bullet('Reduce friction in finding verified vocational courses.', indent=20)
pdf.add_bullet('Increase enrollment rates for "Job Guaranteed" programs.', indent=20)
pdf.add_bullet('Provide a transparent application tracking system.', indent=20)

# 3. Target Audience
pdf.chapter_title('3. Target Audience')
pdf.add_bullet('Primary: Rural youth (Ages 18-30) seeking employment.')
pdf.add_bullet('Secondary: Vocational training institutes and regional skill centers.')

# 4. Key Features
pdf.chapter_title('4. Key Features')
pdf.set_font('Helvetica', 'B', 12)
pdf.cell(0, 10, '4.1 Course Discovery Engine', ln=True)
pdf.add_bullet('Trade-Based Filtering: Filter courses by industry.')
pdf.add_bullet('Duration Categories: Short-term vs. Long-term courses.')
pdf.add_bullet('Job Guarantee Badge: Visual highlight for placement assurance.')

pdf.ln(2)
pdf.set_font('Helvetica', 'B', 12)
pdf.cell(0, 10, '4.2 Training Center Locator', ln=True)
pdf.add_bullet('Map Integration: Google Maps real-time location.')
pdf.add_bullet('Direct Interaction: One-tap Call Center and Directions.')
pdf.add_bullet('District Filters: Search centers in specific districts.')

pdf.ln(2)
pdf.set_font('Helvetica', 'B', 12)
pdf.cell(0, 10, '4.3 Application & Profile System', ln=True)
pdf.add_bullet('Profile Completion Tracking: Visual progress bar.')
pdf.add_bullet('Candidate Summary: Pre-application verification step.')
pdf.add_bullet('Application History: Persistent record synced with Firebase.')

# 5. Technical Specifications
pdf.chapter_title('5. Technical Specifications')
pdf.add_bullet('Mobile Platform: Native Android (Kotlin).')
pdf.add_bullet('Backend Infrastructure: Firebase (Auth, Firestore, Functions).')
pdf.add_bullet('Location Services: Google Maps SDK.')

# 6. User Flow
pdf.chapter_title('6. User Flow')
pdf.chapter_body('1. Login/Register: Secure email authentication.\n2. Explore: Browse vocational catalog with smart filters.\n3. Select: View detailed course and center information.\n4. Apply: Review profile details and submit application.\n5. Track: Monitor application status in My Profile.')

# 7. Roadmap
pdf.chapter_title('7. Roadmap')
pdf.add_bullet('Phase 1: Vocational catalog and basic application flow (Completed).')
pdf.add_bullet('Phase 2: Real-time notifications and call center integration (Completed).')
pdf.add_bullet('Phase 3: Placement tracking and alumni community features (Upcoming).')

output_path = "Namma_Skill_PRD.pdf"
pdf.output(output_path)
print(f"PDF generated successfully: {output_path}")
