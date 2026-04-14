# User Avatar Implementation Guide

## Overview

This document provides a complete implementation guide for displaying user avatars in the top-right corner of the Lab Management System interface.

## Features Implemented

### 1. **Avatar Display System**
- Automatic avatar rendering in header
- Support for multiple avatar formats (Base64, URL, local files)
- Fallback to default avatar
- Responsive design for mobile devices

### 2. **Avatar Upload System**
- Drag-and-drop file upload
- Image preview before upload
- File size and type validation
- Progress indication

### 3. **User Interface**
- Dropdown menu with user actions
- Profile information display
- Settings and logout options
- Dark mode support

## File Structure

```
src/main/resources/
|-- static/
|   |-- css/
|   |   `-- user-avatar.css          # Avatar styles
|   |-- js/
|   |   `-- user-avatar.js           # Avatar functionality
|   `-- images/
|       |-- default-avatar.png       # Default avatar image
|       |-- avatars/                 # User uploaded avatars
|       `-- README.md                # Image documentation
`-- templates/
    `-- user-avatar-template.html    # HTML template
```

## Implementation Steps

### Step 1: Backend API Integration

The avatar system integrates with existing backend APIs:

#### Current User API
```javascript
// GET /lab/auth/current
// Returns: { userId, userName, userAccount, role, avatar }
```

#### Update User API
```javascript
// PUT /lab/users/{userId}
// Body: { avatar: "new_avatar_url_or_base64" }
```

### Step 2: Frontend Integration

#### 1. Include CSS and JavaScript
```html
<link rel="stylesheet" href="/static/css/user-avatar.css">
<script src="/static/js/user-avatar.js"></script>
```

#### 2. Add Avatar Container
```html
<div id="user-avatar-container">
    <!-- Avatar will be rendered here -->
</div>
```

#### 3. Initialize Avatar System
```javascript
// Automatic initialization on page load
document.addEventListener('DOMContentLoaded', () => {
    initUserAvatar();
});
```

### Step 3: Customization Options

#### CSS Customization
```css
/* Override avatar size */
.user-avatar {
    width: 50px;
    height: 50px;
}

/* Customize colors */
.user-name {
    color: #your-color;
}

.user-role {
    background: #your-gradient;
}
```

#### JavaScript Customization
```javascript
// Custom avatar URL processing
function getAvatarDisplayUrl(avatarUrl) {
    // Add custom logic here
    return processedUrl;
}
```

## API Response Format

### Current User Response
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 1,
        "userName": "John Doe",
        "userAccount": "john001",
        "role": "student",
        "avatar": "data:image/png;base64,iVBORw0KGgoAAAANS..."
    }
}
```

### Update Avatar Response
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 1,
        "userName": "John Doe",
        "userAccount": "john001",
        "role": "student",
        "avatar": "data:image/png;base64,iVBORw0KGgoAAAANS..."
    }
}
```

## Avatar Data Types

### 1. Base64 Image Data
```javascript
// Example
const base64Avatar = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
```

### 2. External URL
```javascript
// Example
const urlAvatar = "https://example.com/avatars/user123.jpg";
```

### 3. Local File Reference
```javascript
// Example
const localAvatar = "user123.jpg";
```

## Error Handling

### Common Error Scenarios

#### 1. No Authentication Token
```javascript
// Error: No authentication token found
// Solution: Redirect to login page
```

#### 2. Avatar Load Error
```javascript
// Error: Image fails to load
// Solution: Fallback to default avatar
```

#### 3. Upload Validation Error
```javascript
// Error: Invalid file type or size
// Solution: Show user-friendly error message
```

## Performance Considerations

### 1. Image Optimization
- Compress uploaded images
- Use appropriate image formats
- Implement lazy loading

### 2. Caching Strategy
- Cache avatar URLs locally
- Implement browser caching headers
- Use CDN for external images

### 3. Loading States
- Show loading indicators
- Implement skeleton screens
- Provide smooth transitions

## Security Considerations

### 1. File Upload Security
- Validate file types
- Limit file sizes
- Scan for malware
- Sanitize file names

### 2. Data Protection
- Secure API endpoints
- Validate user permissions
- Implement rate limiting

## Browser Compatibility

### Supported Browsers
- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

### Polyfills Required
- Fetch API (for older browsers)
- Promise support
- ES6 features

## Testing

### Unit Tests
```javascript
// Test avatar rendering
test('renders user avatar correctly', () => {
    const user = { userName: 'Test User', avatar: null };
    renderUserAvatar(user);
    expect(document.querySelector('.user-avatar')).toBeTruthy();
});

// Test avatar upload
test('uploads avatar successfully', async () => {
    const success = await updateUserAvatar('test-avatar.png');
    expect(success).toBe(true);
});
```

### Integration Tests
- Test API integration
- Test error scenarios
- Test responsive behavior

## Deployment

### Production Setup
1. Ensure static files are served correctly
2. Configure proper MIME types
3. Set up CDN for static assets
4. Configure caching headers

### Environment Variables
```javascript
const API_BASE_URL = process.env.API_BASE_URL || '/lab';
const MAX_AVATAR_SIZE = process.env.MAX_AVATAR_SIZE || 5 * 1024 * 1024;
```

## Troubleshooting

### Common Issues

#### Avatar Not Displaying
1. Check if user is logged in
2. Verify API response format
3. Check console for errors
4. Verify image URL validity

#### Upload Not Working
1. Check file size limits
2. Verify file type validation
3. Check network connectivity
4. Verify API endpoint availability

#### Styling Issues
1. Check CSS file loading
2. Verify class names
3. Check for CSS conflicts
4. Test responsive breakpoints

## Future Enhancements

### Planned Features
1. **Avatar Cropping Tool**: Allow users to crop uploaded images
2. **Avatar History**: Keep track of avatar changes
3. **Gravatar Integration**: Support for Gravatar avatars
4. **Avatar Effects**: Add filters and effects to avatars
5. **Batch Upload**: Upload multiple avatars at once

### Performance Improvements
1. **WebP Support**: Serve WebP images for better compression
2. **Progressive Loading**: Load low-res versions first
3. **Smart Caching**: Implement intelligent caching strategies
4. **Image Optimization**: Automatic image optimization

## Support

For issues related to the avatar system:
1. Check this documentation first
2. Review browser console for errors
3. Test with different image formats
4. Contact development team if needed

---

**Last Updated**: 2026-04-11
**Version**: 1.0.0
**Author**: Lab Management System Team
