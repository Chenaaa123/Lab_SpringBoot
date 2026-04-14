# Static Images Directory

This directory contains static images used by the Lab Management System.

## Avatar Images

### Default Avatar
- **File**: `default-avatar.png`
- **Purpose**: Default user avatar when no avatar is set
- **Size**: 200x200px
- **Format**: PNG

### Avatar Storage
- **Directory**: `avatars/` (create if needed)
- **Purpose**: User uploaded avatar files
- **Supported Formats**: PNG, JPG, JPEG, GIF
- **Max Size**: 5MB

## Image Requirements

### User Avatar Specifications
- **Recommended Size**: 200x200px
- **Aspect Ratio**: 1:1 (square)
- **File Size**: Maximum 5MB
- **Supported Formats**: 
  - PNG (recommended for transparency)
  - JPG/JPEG (recommended for photos)
  - GIF (for animated avatars)

### Display Sizes
- **Header Display**: 40x40px
- **Profile Display**: 60x60px
- **Upload Preview**: 100x100px

## Usage

### Frontend Integration
```html
<!-- Include CSS -->
<link rel="stylesheet" href="/static/css/user-avatar.css">

<!-- Avatar Container -->
<div id="user-avatar-container">
    <!-- Avatar will be rendered here -->
</div>

<!-- Include JavaScript -->
<script src="/static/js/user-avatar.js"></script>
```

### API Integration
The avatar system integrates with the following API endpoints:

1. **GET `/lab/auth/current` - Get current user info
2. **PUT `/lab/users/{userId}` - Update user avatar

### Avatar Data Types
The system supports multiple avatar data formats:

1. **Base64 Data**: `data:image/png;base64,iVBORw0KGgo...`
2. **External URL**: `https://example.com/avatar.jpg`
3. **Local File**: `filename.jpg` (stored in avatars/ directory)

## Implementation Notes

### Error Handling
- Automatic fallback to default avatar on load error
- Loading states with shimmer effect
- Error states with visual indicators

### Performance
- Lazy loading of avatar images
- Caching of avatar URLs
- Optimized image sizes for different display contexts

### Security
- File type validation for uploads
- Size limits to prevent abuse
- Sanitization of file names

### Accessibility
- Alt text for screen readers
- Keyboard navigation support
- High contrast support
